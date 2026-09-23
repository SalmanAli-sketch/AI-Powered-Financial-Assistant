@echo off
setlocal

rem Resolve script directory
set "SCRIPT_DIR=%~dp0"
set "WRAPPER_JAR=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.jar"

rem Download the wrapper jar if missing
if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri \"https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar\" -OutFile \"%WRAPPER_JAR%\""
)

rem Execute Maven via the wrapper
java -jar "%WRAPPER_JAR%" %*
