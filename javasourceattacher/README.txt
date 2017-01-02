Local Testing
1. Use command mvn appengine:devserver -Dmaven.test.skip=true

Deploy this app to Google App Engine:

1. Use command mvn install to upload the application to Google App Engine server. Password: wcqsbarqsmnvpxdm
2. Test by browsing to http://javasourceattacher.appspot.com

Developing with Eclipse:

1. Install 'Google Plugin for Eclipse' to your Eclipse IDE (update site: http://dl.google.com/eclipse/plugin/3.4)
2. Use command
mvn package eclipse:eclipse
to create Eclipse project files
3. Import project to Eclipse using 'File/Import/Import Existing projects to Workspace')
4. Add classpath variable M2_REPO to point to <your home>/.m2/repository
