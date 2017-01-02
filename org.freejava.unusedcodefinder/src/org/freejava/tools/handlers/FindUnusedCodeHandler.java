package org.freejava.tools.handlers;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler class for FindRootFilesHandler action.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class FindUnusedCodeHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public FindUnusedCodeHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		// Find selected projects
		final Set<IProject> projects = getSelectedProjects(selection);

		// Create indexes for all projects
		final Map<IProject, File> indexes = createIndexes(projects);

		// Find elements which occur only once
		final List<IModelElement> unusedElements = new ArrayList<IModelElement>();
		try {
			final Set<String> analyzedIds = new HashSet<String>();
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator
					.hasNext();) {
				Object aSelection = iterator.next();
				if (aSelection instanceof IModelElement) {
					IModelElement elem = (IModelElement) aSelection;
					elem.accept(new IModelElementVisitor() {
						public boolean visit(IModelElement e) {
							switch (e.getElementType()) {
							case IModelElement.SOURCE_MODULE:
								// file
								ISourceModule sourceModule = (ISourceModule) e;
								log("Analyzing file: "
										+ sourceModule.getPath().toFile()
												.getAbsolutePath());
								break;
							case IModelElement.TYPE:
								// class
								IType type = (IType) e;
								log("Analyzing class: " + type.getElementName());
								break;
							case IModelElement.FIELD:
								// field
								// IField field = (IField) e;
								// String name = field.getElementName();
								break;
							case IModelElement.METHOD:
								// method
								IMethod method = (IMethod) e;
								String name2 = method.getElementName();
								if ("__construct".equals(name2)
										|| "__destruct".equals(name2)
										|| "__get".equals(name2)
										|| "__set".equals(name2)
										|| "__call".equals(name2)
										|| "__toString".equals(name2)
										|| "__clone".equals(name2)) {
									// ignore special method names
								} else if (foundMethodOnce(method, projects, indexes)) {
									unusedElements.add(method);
								}
								break;
							}
							return true;
						}
					});
				}
			}
		} catch (Exception e) {
			log("Runtime error", e);
		}

		// Print out the elements which occur only once
		if (unusedElements.isEmpty()) {
			log("RESULT: No unused code found.");
		} else {
			log("RESULT: Unused code method/fields found: "
					+ unusedElements.size());
			for (IModelElement unusedElement : unusedElements) {
				switch (unusedElement.getElementType()) {
				case IModelElement.FIELD:
					// field
					IField field = (IField) unusedElement;
					log("Unused field:" + field.getElementName() + "; file: "
							+ field.getSourceModule().getPath());
					break;
				case IModelElement.METHOD:
					IMethod method = (IMethod) unusedElement;
					log("Unused method: " + method.getElementName()
							+ "; file: " + method.getSourceModule().getPath());
					break;
				}
			}
		}

		return null;
	}

	private Map<IProject, File> createIndexes(Set<IProject> projects) {
		Map<IProject, File> result = new Hashtable<IProject, File>();
		for (IProject project: projects) {
			File projectPath = project.getLocation().toFile();
			String projectName = project.getName();
			String tempDir = System.getProperty("java.io.tmpdir");
			File tmpDir = new File(tempDir, "unusedcodefinder");
			tmpDir.mkdirs();
			File indexDir = new File(tmpDir, projectName);
			IndexFiles.createIndex(indexDir, projectPath);
			result.put(project, indexDir);
		}
		return result;
	}

	private Set<IProject> getSelectedProjects(ISelection selection) {
		Set<IProject> projects = new HashSet<IProject>();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		for (Iterator<?> iterator = structuredSelection.iterator(); iterator
				.hasNext();) {
			Object aSelection = iterator.next();
			if (aSelection instanceof IModelElement) {
				IModelElement modelElement = (IModelElement) aSelection;
				projects.add(modelElement.getScriptProject().getProject());
			} else if (aSelection instanceof IResource) {
				IResource resource = (IResource) aSelection;
				projects.add(resource.getProject());
			}
		}
		return projects;
	}

	private boolean foundMethodOnce(IMethod method, Set<IProject> projects, Map<IProject, File> indexes) {
		boolean result;
		String name = method.getElementName();
		if (name.endsWith("Action")) {
			name = name.substring(0, name.length() - "Action".length());
		}
		int count = 0;
		outer:
		for (IProject aproject : projects) {
			IndexSearcher searcher = null;
			try {
				String field = "contents";
				searcher = new IndexSearcher(FSDirectory.open(indexes.get(aproject)));
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
				QueryParser parser = new QueryParser(Version.LUCENE_31, field, analyzer);
				Query query = parser.parse(name);
				TopDocs results = searcher.search(query, 100);
			    ScoreDoc[] hits = results.scoreDocs;
				for (int i = 0; i < hits.length; i++) {
			        Document doc = searcher.doc(hits[i].doc);
			        String path = doc.get("path");
			        String content = FileUtils.readFileToString(new File(path));
			        count += StringUtils.countMatches(content, name);
			        if (count > 1) break outer;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (searcher != null) searcher.close();
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}
		result = (count <= 1);


		/*
		if (analyzedIds.contains(method.getElementName())) {
			result = false;
		} else {
			analyzedIds.add(name);

			// Index Search
			final List<SearchMatch> found1 = new ArrayList<SearchMatch>();
			try {
				SearchRequestor requestor1 = new SearchRequestor() {
					public void acceptSearchMatch(SearchMatch match)
							throws CoreException {
						found1.add(match);
					}
				};
				SearchPattern pattern1 = SearchPattern.createPattern(method,
						IDLTKSearchConstants.REFERENCES);
				IDLTKSearchScope scope1 = SearchEngine
						.createSearchScope(project);
				SearchEngine engine1 = new SearchEngine();
				engine1.search(pattern1, new SearchParticipant[] { SearchEngine
						.getDefaultSearchParticipant() }, scope1, requestor1,
						null);
			} catch (Exception e) {
				// ignore
			}

			if (!found1.isEmpty()) {
				result = false;
			} else {
				result = true;
			}

			// Text Search
			if (result) {
				IResource[] rootResources = projects
						.toArray(new IResource[projects.size()]);
				TextSearchEngine engine = TextSearchEngine.create();
				TextSearchScope scope = TextSearchScope.newSearchScope(
						rootResources, Pattern.compile(".+"), false);
				final List<TextSearchMatchAccess> found = new ArrayList<TextSearchMatchAccess>();
				TextSearchRequestor requestor = new TextSearchRequestor() {
					public boolean acceptPatternMatch(
							TextSearchMatchAccess matchAccess)
							throws CoreException {
						found.add(matchAccess);
						return (found.size() > 1) ? false : true;
					}
				};
				if (name.endsWith("Action")) {
					name = name.substring(0, name.length() - "Action".length());
				}
				Pattern searchPattern = Pattern.compile("\\b"
						+ name.replace("$", "\\$") + "\\b");
				engine.search(scope, requestor, searchPattern, null);
				if (found.size() > 1) {
					if (name.equals("indexnew")) {
						System.out.println("---indexnew:false" + found.size());
					}
					result = false;
				} else {
					result = true;
					if (name.equals("indexnew")) {
						System.out.println("---indexnew:true" + found.size());
					}
				}
			}

		}*/
		return result;
	}

	private void log(Object message, Exception e) {
		MessageConsole con = findConsole("Find Unused Code Console");
		MessageConsoleStream out = con.newMessageStream();
		out.println(message.toString());
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		out.print(sw.getBuffer().toString());
	}

	private void log(Object message) {
		MessageConsole con = findConsole("Find Unused Code Console");
		MessageConsoleStream out = con.newMessageStream();
		out.println(message.toString());
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
