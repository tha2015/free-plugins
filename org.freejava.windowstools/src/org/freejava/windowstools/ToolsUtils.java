package org.freejava.windowstools;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ToolsUtils {

	public static List<File> structuredSelectionToOsPathList(
			IStructuredSelection structuredSelection, ExecutionEvent event)
			throws ExecutionException {
		List<File> fileList = new ArrayList<File>();
		for (Iterator<Object> i = getIterator(structuredSelection); i
				.hasNext();) {
			Object selectedObject = i.next();
			if (!(selectedObject instanceof IResource || (selectedObject instanceof IAdaptable && ((IAdaptable) selectedObject)
					.getAdapter(IResource.class) != null))) {
				continue;
			}
			IResource resource;
			if (selectedObject instanceof IResource) {
				resource = (IResource) selectedObject;
			} else {
				resource = (IResource) ((IAdaptable) selectedObject)
						.getAdapter(IResource.class);
				assert resource != null;
			}
			IPath path = resource.getLocation();
			if (path == null) {
				continue;
			}
			String pathString = path.toOSString();
			File file = getPathChecker().checkPath(pathString,
					getResourceType(), event);
			if (file != null) {
				fileList.add(file);
			}
		}
		return fileList;
	}

	private static Iterator<Object> getIterator(
			IStructuredSelection structuredSelection) {
		return structuredSelection.iterator();
	}

	private static PathChecker.ResourceType getResourceType() {
		return PathChecker.ResourceType.BOTH;
	}

	private static PathChecker getPathChecker() {
		return new PathChecker();
	}

	private static class PathChecker
	{
	  /**
	   * Type of a filesystem resource, either file or directory.
	   */
	  public static enum ResourceType
	  {
	    /**
	     * Resource of type file (in contrast to directory).
	     */
	    FILE,

	    /**
	     * Resource of type directory (in contrast to file).
	     */
	    DIRECTORY,

	    /**
	     * File or directory.
	     */
	    BOTH

	  }


	  public PathChecker()
	  {
	  }


	  /**
	   * Checks if the <code>pathString</code> is a valid filesystem path.
	   *
	   * @param pathString a String meant to be a filesystem path
	   * @param resourceType either ResourceType.FILE or ResourceType.DIRECTORY,
	   *          depending on which resource type is expected or ResourceType.BOTH,
	   *          if both resource types are acceptable
	   * @param event the ExecutionEvent in which's context pathString occured
	   * @return the absolute path of the file specified by <code>pathString</code>
	   *         or <code>null</code> if <code>pathString</code> does not point
	   *         to a valid file/directory.
	   * @throws ExecutionException this method calls {@link
	   *           org.eclipse.ui.handlers.HandlerUtil#getActiveShellChecked(ExecutionEvent)}
	   *           with the given <code>event</code>, this method is declared to
	   *           throw ExecutionException.
	   */
	  public File checkPath(String pathString, ResourceType resourceType,
	      ExecutionEvent event)
	  {
	    if (pathString == null)
	    {
	      throw new IllegalArgumentException("pathString is null");
	    }
	    if (resourceType == null)
	    {
	      throw new IllegalArgumentException("resourceType is null");
	    }
	    if (event == null)
	    {
	      throw new IllegalArgumentException("event is null");
	    }
	    File file = new File(pathString);
	    if (!file.exists())
	    {
	      File parentFile = file.getParentFile();
	      if (parentFile == null)
	      {

	        return null;
	      }
	      if (!parentFile.exists())
	      {

	        return null;
	      }
	      file = parentFile;
	    }
	    if (resourceType == ResourceType.DIRECTORY && !file.isDirectory())
	    {
	      File parentFile = file.getParentFile();
	      if (parentFile == null)
	      {

	        return null;
	      }
	      file = parentFile;
	    }
	    if (resourceType == ResourceType.FILE && !file.isFile())
	    {

	      return null;
	    }
	    return file;
	  }
	}
}
