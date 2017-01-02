package org.freejava.tools.handlers.classpathutil;

import org.eclipse.jdt.internal.ui.JavaPlugin;

public class Logger {
    public static void debug(String message, Throwable throwable) {
        try {
            if (message != null) JavaPlugin.getDefault().logErrorMessage(message);
            if (throwable != null) JavaPlugin.getDefault().log(throwable);
        } catch (Exception e) {
            if (message != null) System.err.println(message);
            if (throwable != null) throwable.printStackTrace();
        }
    }
}
