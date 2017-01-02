call mvn package

java -cp "D:\programs\appengine-java-sdk-1.6.1\lib\appengine-tools-api.jar" com.google.appengine.tools.KickStart --jvm_flag=-Xdebug --jvm_flag=-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y com.google.appengine.tools.development.DevAppServerMain target\javasourceattacher


