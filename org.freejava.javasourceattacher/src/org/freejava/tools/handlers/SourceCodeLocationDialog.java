package org.freejava.tools.handlers;


import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.UrlValidator;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.freejava.tools.handlers.classpathutil.Logger;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class SourceCodeLocationDialog extends TitleAreaDialog {
    private Map<String, IPackageFragmentRoot> requests;
    private IProgressMonitor monitor;

    private Text[] binaries;
    private Text[] sources;
    private String[] libPaths;
    private SourceCodeLocationDialogModel model;

    public SourceCodeLocationDialog(Map<String, IPackageFragmentRoot> requests, IProgressMonitor monitor, Shell parentShell, String[] sourcePaths) {
        super(parentShell);
        this.requests = requests;
        this.monitor = monitor;

        this.libPaths = sourcePaths;
        this.model = new SourceCodeLocationDialogModel(this.libPaths);

        this.binaries = new Text[libPaths.length];
        this.sources = new Text[libPaths.length];
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Library Source Location");
    }

    @Override
    protected Control createContents(Composite parent) {
        Control container = super.createContents(parent);
        setTitle("Library Source Location");
        setMessage("Sorry but source code cannot be found in our database. You can help the community by providing URL location of source code archive below.");
        return container;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        createForm(parent);

        return container;
    }

    private Composite createForm(Composite parent) {

        // binding values to model
        DataBindingContext dbc = new DataBindingContext();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Link link;
        Label label;
        for (int i = 0; i < libPaths.length; i++) {
            GridData gridData;

            gridData = new GridData();
            gridData.widthHint = 120;
            link = new Link(composite, SWT.LEFT);
            link.setText("Library (<a>Search by MD5 digest</a>):");
            link.setLayoutData(gridData);

            binaries[i] = new Text(composite, SWT.SINGLE | SWT.BORDER);
            gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            gridData.widthHint = 400;
            gridData.horizontalIndent = 5;
            binaries[i].setLayoutData(gridData);
            binaries[i].setText(libPaths[i]);
            binaries[i].setToolTipText("Do not change this value.");

            IObservableValue modelObservable1 = new IndexedPropertyObservableValue(model, "binaries", i);
            ISWTObservableValue swtObservable1 = SWTObservables.observeText(binaries[i], SWT.Modify);
            IValidator validator1 = new IValidator() {
                public IStatus validate(Object value) {
                    if (value instanceof String) {
                        String s = value.toString();
                         String[] schemes = {"http","https"};
                         UrlValidator urlValidator = new UrlValidator(schemes);
                        if (StringUtils.isNotBlank(s) && (urlValidator.isValid(s) || new File(s).exists())) {
                            return ValidationStatus.ok();
                        }
                    }
                    return ValidationStatus.error("Not a valid file path.");
                }
            };
            UpdateValueStrategy strategy1 = new UpdateValueStrategy();
            strategy1.setBeforeSetValidator(validator1);
            Binding bindValue1 = dbc.bindValue(swtObservable1, modelObservable1, strategy1, null);
            // Add some decorations
            try {
	            MethodUtils.invokeExactStaticMethod(Class.forName("org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport"),
                    "create", new Object[]{bindValue1, SWT.TOP | SWT.LEFT}, new Class[] {
            		Class.forName("org.eclipse.core.databinding.ValidationStatusProvider"), int.class} );
            } catch (Exception e) {
                Logger.debug("ignored!", e);
            }

            final Text fileText = binaries[i];
            link.addListener (SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    try {
                        File file = new File(fileText.getText());
            			String md5 = Files.hash(file, Hashing.md5()).toString();
                        URL url = new URL( "http://www.google.com/search?q=" + URLEncoder.encode(md5, "UTF-8"));
                        int style = IWorkbenchBrowserSupport.AS_EXTERNAL
                                | IWorkbenchBrowserSupport.NAVIGATION_BAR;
                        IWorkbenchBrowserSupport wbbs = PlatformUI.getWorkbench()
                                .getBrowserSupport();
                        IWebBrowser browser = wbbs.createBrowser(style, "md5search",
                                "MD5 Search", "MD5 Search");
                        browser.openURL(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            IObservableValue modelObservable = new IndexedPropertyObservableValue(model, "binaries", i);
            ISWTObservableValue swtObservable = SWTObservables.observeText(binaries[i], SWT.Modify);
            dbc.bindValue(swtObservable, modelObservable, null, null);

            gridData = new GridData();
            gridData.widthHint = 120;
            label = new Label(composite, SWT.LEFT);
            label.setText("Source URL (jar or zip):");
            label.setLayoutData(gridData);

            sources[i] = new Text(composite, SWT.SINGLE | SWT.BORDER);
            gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            gridData.widthHint = 400;
            gridData.horizontalIndent = 5;
            sources[i].setLayoutData(gridData);
            sources[i].setToolTipText("URL for a zip file or a jar file which contains source code for the above library.\n "
            + "For example: http://archive.apache.org/dist/tomcat/tomcat-5/v5.5.20/src/apache-tomcat-5.5.20-src.zip");

            IObservableValue modelObservable2 = new IndexedPropertyObservableValue(model, "sources", i);
            ISWTObservableValue swtObservable2 = SWTObservables.observeText(sources[i], SWT.Modify);
            IValidator validator2 = new IValidator() {
                public IStatus validate(Object value) {
                    if (value instanceof String) {
                        String s = value.toString();
                         String[] schemes = {"http","https", "ftp"};
                        if (StringUtils.isBlank(s) || StringUtils.startsWith(s, "scm:") || new UrlValidator(schemes).isValid(s)) {
                            return ValidationStatus.ok();
                        }
                    }
                    return ValidationStatus.error("Not a valid URL or Maven SCM URL");
                }
            };
            UpdateValueStrategy strategy2 = new UpdateValueStrategy();
            strategy2.setBeforeSetValidator(validator2);
            Binding bindValue2 = dbc.bindValue(swtObservable2, modelObservable2, strategy2, null);
            // Add some decorations
            try {
                MethodUtils.invokeExactStaticMethod(Class.forName("org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport"),
                        "create", new Object[]{bindValue2, SWT.TOP | SWT.LEFT}, new Class[] {
            			Class.forName("org.eclipse.core.databinding.ValidationStatusProvider"), int.class});
            } catch (Exception e) {
                Logger.debug("ignored!", e);
            }

            if (i == 0) sources[i].setFocus();

        }



        return composite;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control buttonBar = super.createButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(true);
        return buttonBar;
    }

    @Override
    protected void okPressed() {
        // close dialog
        super.okPressed();

        // prepare data for Job
        final Map<String, String> bin2srcMap = new HashMap<String, String>();
        for (int i = 0; i < this.model.getBinaries().length; i++) {
            String binPath = this.model.getBinaries()[i];
            String srcUrl = this.model.getSources()[i];
            if (StringUtils.isNotBlank(binPath) && StringUtils.isNotBlank(srcUrl)) {
                bin2srcMap.put(binPath, srcUrl);
            }
        }

        Job job = new Job("Trying to attach source to library again (after source contribution)...") {
            protected IStatus run(IProgressMonitor monitor) {
                List<IPackageFragmentRoot> nextRoots = new ArrayList<IPackageFragmentRoot>();
                for (Map.Entry<String, String> entry : bin2srcMap.entrySet()) {
                    String binPath = entry.getKey();
                    String srcUrl = entry.getValue();
                    try {
                        boolean success = SourceCheck.proposeSourceLink(binPath, srcUrl);
                        if (success && requests.containsKey(binPath)) {
                            nextRoots.add(requests.get(binPath));
                        }
                    } catch (Exception e) {
                    }
                }

                // retry
                if (!nextRoots.isEmpty()) {
                    return JavaSourceAttacherHandler.updateSourceAttachments(nextRoots, monitor, getShell(), false);
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.LONG);
        job.schedule();
    }

}
