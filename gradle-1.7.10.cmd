@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE_USER_HOME=%~dp0work\.gradle-user-home"
call "%~dp0work\tools\gradle-2.14.1\bin\gradle.bat" %*
