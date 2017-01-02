package org.freejava.windowstools;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class StartCmdHandler extends AbstractHandler {

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection sel = (IStructuredSelection) selection;
        List<File> files = ToolsUtils.structuredSelectionToOsPathList(sel, event);
        if (!files.isEmpty()) {
            try {
            	File file = files.get(0);
            	File dir = file.isDirectory() ? file : file.getParentFile();
			    Process child = Runtime.getRuntime().exec(new String[]{
			    		"cmd.exe",
			    		"/c",
			    		"start",
			    		"cmd.exe",
			    		"cd",
			    		"/d",
			    		"\"" + dir.getAbsolutePath() + "\""}, null, dir);
            } catch (Exception e) {
    			// TODO: handle exception
    		}

        }
        return null;
    }

}
