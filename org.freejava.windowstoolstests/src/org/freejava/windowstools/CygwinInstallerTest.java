package org.freejava.windowstools;

import org.junit.Test;

public class CygwinInstallerTest {

    @Test
    public void testInstall() {
        CygwinInstaller obj = new CygwinInstaller();
        obj.install("C:\\1");
    }

}
