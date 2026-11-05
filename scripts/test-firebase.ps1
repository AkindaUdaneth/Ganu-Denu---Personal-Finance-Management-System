$ErrorActionPreference = 'Stop'
$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:PATH
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $adbPath = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
    $deviceState = & $adbPath shell dumpsys user 2>&1
    if ($LASTEXITCODE -ne 0 -or ($deviceState -join "`n") -notmatch 'RUNNING_UNLOCKED') {
        throw 'Connect one Android emulator and unlock it before running the test suite.'
    }
    $env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
    $env:PATH = "$env:JAVA_HOME/bin;$env:PATH"
    & ./gradlew.bat --no-daemon '-Dorg.gradle.jvmargs=-Xmx1024m' '-Pkotlin.compiler.execution.strategy=in-process' :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Build or unit tests failed.' }
    & npx.cmd --yes firebase-tools@15.35.0 emulators:exec --project demo-ganudenu-qa --config firebase.test.json --only 'auth,firestore' 'powershell -NoProfile -File scripts/run-android-tests.ps1'
    if ($LASTEXITCODE -ne 0) { throw 'Firebase integration tests failed.' }
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:PATH = $previousPath
    Pop-Location
}
