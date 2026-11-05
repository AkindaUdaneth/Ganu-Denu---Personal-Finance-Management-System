# Auth and income regression checks

Connect an Android emulator, then run from the repository root in PowerShell.

```powershell
./scripts/test-firebase.ps1
```

This starts local Firebase Auth and Firestore with the rules in `firestore.rules`, runs the unit and Android test suites, and stops the services. The test project is `demo-ganudenu-qa`. Named Firebase clients keep SDK integration tests separate from the application's configured Firebase project. The first run downloads the Firebase CLI and emulator binaries. Java 21 is supplied by Android Studio's bundled JBR.

The runner completes the build in a temporary 1 GB JVM, then releases that process before Android instrumentation starts. This reduces memory pressure when Android Studio and the emulator run together. Instrumentation output is checked for JUnit success and saved to `scripts/temp/android-tests.log`. If the Firebase services are already running, use `./scripts/run-android-tests.ps1` after building the test APK.

`FirebaseRepositoryIntegrationTest` covers profile creation and editing with server timestamps, duplicate email, wrong password, disabled users, password-reset action codes, year-boundary queries, offline cache and pending writes, reconnect, duplicate-safe document IDs and access rules. Reset codes are obtained from the local Auth emulator. Delivery to a real mailbox and the deployed production configuration require a separate live acceptance check.

`AuthErrorUiTest` checks error presentation and retry. `IncomeRecoveryUiTest` selects a calendar date, verifies a denied save can be retried, and parcels saved state into a new ViewModel to simulate process restoration. It preserves the draft ID, source, amount, date, frequency, status and selected month. This does not force-kill an Android process.

`AuthIncomeVisualTest` compares four fixed 360 by 900 pixel captures with reviewed images in `app/src/androidTest/assets/goldens`. Use the Pixel 6 API 37 emulator used for the baselines. A pixel differs when a colour channel changes by more than 16, and the allowed changed area is 0.5%. The baselines describe the current app design. They are not pixel comparisons with the original multi-screen Figma reference.

To capture intentional design changes for review, run the visual class with `-Pandroid.testInstrumentationRunnerArguments.updateGoldens=true`. Review the resulting images under `app/build/outputs/connected_android_test_additional_output`, then copy approved PNGs into the test assets. Ordinary test runs compare images and never update baselines automatically.

Without the local Firebase services, `./gradlew.bat :app:connectedDebugAndroidTest` runs the UI suite and skips the six opt-in backend tests. Unit test HTML reports are under `app/build/reports/tests`, and instrumentation reports are under `app/build/reports/androidTests`.
