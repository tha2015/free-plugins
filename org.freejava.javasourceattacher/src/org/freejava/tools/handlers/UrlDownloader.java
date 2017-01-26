package org.freejava.tools.handlers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Zip;

public class UrlDownloader {

    public String download(String url) throws Exception {
        String result;
        if (StringUtils.startsWith(url, "scm:")) {
            result = downloadFromScm(url);
        } else if (new File(url).exists()) {
            result = url;
        } else {
            result = downloadFromUrl(url);
        }
        return result;
    }


    private String downloadFromScm(String url) throws Exception {
        String result;
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File checkoutDirectory = new File(tmpDir, "ssourceattacher_" + DigestUtils.md5Hex(url));
        File file = new File(tmpDir, "ssourceattacher_" + DigestUtils.md5Hex(url) + ".zip");
        if (!file.exists()) {
            if (!checkoutDirectory.exists() || checkoutDirectory.list().length == 0) { // not exist or empty

                // see http://maven.apache.org/scm/guide/usage.html
                // http://maven.apache.org/scm/scm-url-format.html
                ScmManager scmManager = new BasicScmManager();
                scmManager.setScmProvider("accurev", new org.apache.maven.scm.provider.accurev.AccuRevScmProvider());
                scmManager.setScmProvider("bazaar", new org.apache.maven.scm.provider.bazaar.BazaarScmProvider());
                scmManager.setScmProvider("clearcase", new org.apache.maven.scm.provider.clearcase.ClearCaseScmProvider());
                scmManager.setScmProvider("hg", new org.apache.maven.scm.provider.hg.HgScmProvider());
                scmManager.setScmProvider("local", new org.apache.maven.scm.provider.local.LocalScmProvider());
                scmManager.setScmProvider("perforce", new org.apache.maven.scm.provider.perforce.PerforceScmProvider());
                scmManager.setScmProvider("cvs_native", new org.apache.maven.scm.provider.cvslib.cvsexe.CvsExeScmProvider());
                scmManager.setScmProvider("cvs", new org.apache.maven.scm.provider.cvslib.cvsjava.CvsJavaScmProvider());
                scmManager.setScmProvider("git", new org.apache.maven.scm.provider.git.jgit.JGitScmProvider());
                scmManager.setScmProvider("svn", new org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider());
                scmManager.setScmProvider("starteam", new org.apache.maven.scm.provider.starteam.StarteamScmProvider());
                scmManager.setScmProvider("synergy", new org.apache.maven.scm.provider.synergy.SynergyScmProvider());
                scmManager.setScmProvider("vss", new org.apache.maven.scm.provider.vss.VssScmProvider());
                scmManager.setScmProvider("jazz", new org.apache.maven.scm.provider.jazz.JazzScmProvider());

                String scmUrl;
                ScmVersion scmVersion;
                if (url.indexOf('#') != -1) {
                    scmUrl =  StringUtils.trimToEmpty(url.substring(0, url.indexOf('#')));
                    String fragment = StringUtils.trimToEmpty(url.substring(url.indexOf('#') + 1));
                    if (fragment.indexOf('=') != -1) {
                        String[] versionTypeAndVersion = StringUtils.split(fragment, '=');
                        String version = StringUtils.trim(versionTypeAndVersion[1]);
                        String type = StringUtils.trim(versionTypeAndVersion[0]);
                        if ("tag".equalsIgnoreCase(type)) {
                            scmVersion = new ScmTag(version);
                        } else if ("branch".equalsIgnoreCase(type)) {
                            scmVersion = new ScmBranch(version);
                        } else if ("revision".equalsIgnoreCase(type)) {
                            scmVersion = new ScmRevision(version);
                        } else if ("commitId".equalsIgnoreCase(type)) {
                            scmVersion = new ScmTag(version);
                        } else {
                            throw new IllegalArgumentException("'" + type + "' version type isn't known.");
                        }
                    } else { // default is tag
                        scmVersion = new ScmTag(fragment);
                    }
                } else if (url.indexOf(';') != -1) {
                    scmUrl =  StringUtils.trimToEmpty(url.substring(0, url.indexOf(';')));
                    String fragment = StringUtils.trimToEmpty(url.substring(url.indexOf(';') + 1));
                    scmVersion = null;
                    if (fragment.indexOf('=') != -1) {
                    	String[] properties = StringUtils.split(fragment, ';');
                        for (String property : properties) {
                            String[] versionTypeAndVersion = StringUtils.split(property, '=');
                            String version = StringUtils.strip(versionTypeAndVersion[1], " \"");
                            String type = StringUtils.trim(versionTypeAndVersion[0]);
                            if ("tag".equalsIgnoreCase(type)) {
                                scmVersion = new ScmTag(version);
                                break;
                            } else if ("branch".equalsIgnoreCase(type)) {
                                scmVersion = new ScmBranch(version);
                                break;
                            } else if ("revision".equalsIgnoreCase(type)) {
                                scmVersion = new ScmRevision(version);
                                break;
                            } else if ("commitId".equalsIgnoreCase(type)) {
                                scmVersion = new ScmTag(version);
                                break;
                            }
                        }

                    } else { // default is tag
                        scmVersion = new ScmTag(fragment);
                    }
                } else {
                    scmUrl = url;
                    scmVersion = null;
                }
                if (!checkoutDirectory.exists()) {
                    checkoutDirectory.mkdir();
                }
                ScmRepository repository = scmManager.makeScmRepository(scmUrl);
                CheckOutScmResult checkOutResult = scmManager.checkOut(repository, new ScmFileSet(checkoutDirectory), scmVersion);
            }

            if (checkoutDirectory.exists() && checkoutDirectory.list().length > 0) {
                zipFolder(checkoutDirectory, file);
                // do not delete to avoid slowing down
                //delete(checkoutDirectory);
            }
        }

        if (file.exists()) {
            result = file.getAbsolutePath();
        } else {
            result = null;
        }

        return result;
    }


    public void zipFolder(File srcFolder, File destZipFile) {
        Zip zipper = new Zip();
        zipper.setCompress(false);
        zipper.setLevel(0);
        zipper.setDestFile(destZipFile);
        zipper.setBasedir(srcFolder);
        zipper.setTaskName("zip");
        zipper.setTaskType("zip");
        zipper.setProject(new Project());
        zipper.setOwningTarget(new Target());
        zipper.execute();
     }

    public void delete(File folder) {
        Delete delete = new Delete();
        delete.setDir(folder);
        delete.setTaskName("delete");
        delete.setTaskType("delete");
        delete.setProject(new Project());
        delete.setOwningTarget(new Target());
        delete.execute();
    }

    private String downloadFromUrl(String url) throws IOException {
        String result;
        File file = File.createTempFile("ssourceattacher", ".tmp");

        InputStream is = null;
        OutputStream os = null;
        try {
            URLConnection conn = new URL(url).openConnection();
            is = openConnectionCheckRedirects(conn);
            os = FileUtils.openOutputStream(file);
            IOUtils.copy(is, os);
        } catch (Exception e) {
            IOUtils.closeQuietly(os);
            file.delete();
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        result = file.getAbsolutePath();
        return result;
    }

    private InputStream openConnectionCheckRedirects(URLConnection c) throws IOException
    {
       boolean redir;
       int redirects = 0;
       InputStream in = null;
       do
       {
          if (c instanceof HttpURLConnection)
          {
             ((HttpURLConnection) c).setInstanceFollowRedirects(false);
             c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.63 Safari/535.7");
          }

          // We want to open the input stream before getting headers
          // because getHeaderField() et al swallow IOExceptions.
          in = c.getInputStream();
          redir = false;
          if (c instanceof HttpURLConnection)
          {
             HttpURLConnection http = (HttpURLConnection) c;
             int stat = http.getResponseCode();
             if (stat >= 300 && stat <= 307 && stat != 306 &&
                stat != HttpURLConnection.HTTP_NOT_MODIFIED)
             {
                URL base = http.getURL();
                String loc = http.getHeaderField("Location");
                URL target = null;
                if (loc != null)
                {
                   target = new URL(base, loc);
                }
                http.disconnect();
                // Redirection should be allowed only for HTTP and HTTPS
                // and should be limited to 5 redirections at most.
                if (target == null || !(target.getProtocol().equals("http")
                   || target.getProtocol().equals("https"))
                   || redirects >= 5)
                {
                   throw new SecurityException("illegal URL redirect");
                }
                redir = true;
                c = target.openConnection();
                redirects++;
             }
          }
       } while (redir);
       return in;
    }

}
