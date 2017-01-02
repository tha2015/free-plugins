@echo off

if "%OS%"=="Windows_NT" @setlocal

set MAVEN_OPTS=-Xmx512m -XX:MaxPermSize=256m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000

REM use jta.scope to exclude JTA library from dependency list when running Jetty to avoid classloader errors
mvn -Dappengine-local-runtime.scope=runtime -Dappengine-api-stubs.scope=runtime -Djava.util.logging.config.file=war/WEB-INF/logging.properties jetty:run

@endlocal