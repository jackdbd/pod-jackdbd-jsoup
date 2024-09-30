@echo on

@REM Taken from: https://github.com/babashka/pod-babashka-buddy/blob/main/script/compile.bat

if "%GRAALVM_HOME%"=="" (
    echo Please set GRAALVM_HOME
    exit /b
)

if "%JAVA_HOME%"=="" (
    echo Please set JAVA_HOME
    exit /b
)

@REM set JAVA_HOME=%GRAALVM_HOME%
@REM set PATH=%GRAALVM_HOME%\bin;%PATH%
echo GRAALVM_HOME is %GRAALVM_HOME%
echo JAVA_HOME is %JAVA_HOME%

if "%POD_NAME%"=="" (
    echo Please set POD_NAME
    exit /b
)

set POD_NAME=%POD_NAME%
set /P POD_VERSION=< resources\POD_VERSION
echo Building %POD_NAME% version %POD_VERSION%

call %GRAALVM_HOME%\bin\gu.cmd install native-image

call %GRAALVM_HOME%\bin\native-image.cmd "--version"

REM Build the binary with GraalVM native-image
call %GRAALVM_HOME%\bin\native-image.cmd ^
  "-jar" "pod.jackdbd.jsoup" ^
  "-H:+ReportExceptionStackTraces" ^
  "--no-fallback" ^
  "--verbose"

if %errorlevel% neq 0 exit /b %errorlevel%

echo Creating zip archive
jar -cMf %POD_NAME%-%POD_VERSION%-windows-amd64.zip %POD_NAME%.exe
