package org.freejava.tools.handlers.samplesearch;

import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class SampleCodeSearchDialog extends TitleAreaDialog {

	private Text keyword;

	private SampleCodeSearchModel model;

	public SampleCodeSearchDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Sample Code Search");
	}

	@Override
	protected Control createContents(Composite parent) {
		Control container = super.createContents(parent);
		setTitle("Enter keyword");
		setMessage("Your keyword will be searched using online services for Java examples.");
		return container;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		createForm(parent);

		return container;
	}

	private Composite createForm(Composite parent) {

		this.model = new SampleCodeSearchModel();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label;
		GridData gridData;

		// row 1: Keyword

		label = new Label(composite, SWT.LEFT);
		label.setText("Keyword:");
		keyword = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		keyword.setLayoutData(gridData);
		//binding values to model
		DataBindingContext dbc = new DataBindingContext();
		IObservableValue modelObservable = BeansObservables.observeValue(model, "keyword");
		dbc.bindValue(SWTObservables.observeText(keyword, SWT.Modify), modelObservable, null, null);


		// Set OK button status
		keyword.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (e.getSource() == keyword) {
					getButton(IDialogConstants.OK_ID).setEnabled(validate());
				}
			}
		});

		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return buttonBar;
	}

	private boolean validate() {
		String keywordText = keyword.getText();
		boolean result = (!keywordText.trim().equals(""));
		return result;
	}

	@Override
	protected void okPressed() {
		//SampleSearchManager sm = new SampleSearchManager();
		try {
			URL url;

			url = new URL(
					"http://www.google.com/cse?cx=004472050566847039233:9ld3aazskua&ie=UTF-8&q="
							+ URLEncoder.encode(model.getKeyword(), "UTF-8").replaceAll("%20", "+"));

			int style = IWorkbenchBrowserSupport.AS_EDITOR
					| IWorkbenchBrowserSupport.NAVIGATION_BAR;
			IWorkbenchBrowserSupport wbbs = PlatformUI.getWorkbench()
					.getBrowserSupport();
			IWebBrowser browser = wbbs.createBrowser(style, "codesearch",
					"Java examples", "Java examples");
			browser.openURL(url);

//			URL url = new URL(
//			"http://www.google.com/custom?domains=exampledepot.com&q="
//					+ URLEncoder.encode(model.getKeyword(), "UTF-8").replaceAll("%20", "+")
//					+ "&sitesearch=exampledepot.com&client=pub-6001183370374757&forid=1&ie=ISO-8859-1&oe=ISO-8859-1&cof=GALT%3A%23008000%3BGL%3A1%3BDIV%3A%23336699%3BVLC%3A663399%3BAH%3Acenter%3BBGC%3AFFFFFF%3BLBGC%3A336699%3BALC%3A0000FF%3BLC%3A0000FF%3BT%3A000000%3BGFNT%3A0000FF%3BGIMP%3A0000FF%3BFORID%3A1%3B&hl=en");

//            // exampledepot
//			url = new URL(
//					"http://www.google.com/search?q=site%3Awww.exampledepot.com%2Fegs+"
//							+ URLEncoder.encode(model.getKeyword(), "UTF-8").replaceAll("%20", "+"));
//
//			int style = IWorkbenchBrowserSupport.AS_EDITOR
//					| IWorkbenchBrowserSupport.STATUS;
//			IWorkbenchBrowserSupport wbbs = PlatformUI.getWorkbench()
//					.getBrowserSupport();
//			IWebBrowser browser = wbbs.createBrowser(style, "console1",
//					"exampledepot.com", "exampledepot.com");
//			browser.openURL(url);
//
//			// kodejava
//			url = new URL(
//					"http://www.google.com/search?q=site%3Awww.kodejava.org%2Fexamples+"
//							+ URLEncoder.encode(model.getKeyword(), "UTF-8").replaceAll("%20", "+"));
//			style = IWorkbenchBrowserSupport.AS_EDITOR
//					| IWorkbenchBrowserSupport.STATUS;
//			wbbs = PlatformUI.getWorkbench().getBrowserSupport();
//			browser = wbbs.createBrowser(style, "console2", "www.kodejava.org", "www.kodejava.org");
//			browser.openURL(url);

			super.okPressed();
		} catch (Exception e) {
			//throw new ExecutionException("Cannot open search page. Error:" + e.getMessage(), e);
		}
//		if (sm.search(model)) {
//			MessageDialog.openInformation(this.getShell(), "Project Creation", "Project is created successfully!");
//			//super.okPressed();
//		} else {
//			MessageDialog.openInformation(this.getShell(), "Project Creation", "Project could not be created!");
//		}
	}

}
