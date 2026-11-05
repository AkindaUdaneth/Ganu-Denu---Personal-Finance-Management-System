param([string]$TestClass = '')

$ErrorActionPreference = 'Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $adbPath = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
    & $adbPath install -r app/build/outputs/apk/debug/app-debug.apk
    if ($LASTEXITCODE -ne 0) { throw 'Could not install the debug APK.' }
    & $adbPath install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
    if ($LASTEXITCODE -ne 0) { throw 'Could not install the Android test APK.' }
    $instrumentArgs = @('shell', 'am', 'instrument', '-w', '-r', '-e', 'firebaseEmulators', 'true')
    if ($TestClass) { $instrumentArgs += @('-e', 'class', $TestClass) }
    $instrumentArgs += 'com.sliit.se3092.ganudenu.test/androidx.test.runner.AndroidJUnitRunner'
    $result = & $adbPath @instrumentArgs 2>&1
    $exitCode = $LASTEXITCODE
    New-Item -ItemType Directory -Path scripts/temp -Force | Out-Null
    $result | Set-Content scripts/temp/android-tests.log
    $result | Out-Host
    $output = $result -join "`n"
    if ($exitCode -ne 0 -or $output -notmatch 'OK \(\d+ tests?\)' -or $output -match 'FAILURES!!!|INSTRUMENTATION_FAILED') {
        throw 'Android tests failed. See scripts/temp/android-tests.log.'
    }
} finally {
    Pop-Location
}
