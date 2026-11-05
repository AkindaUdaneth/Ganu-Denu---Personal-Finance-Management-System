# Ganu Denu end to end and integration test cases

Execution date: 5 November 2026. Scope: implemented authentication, income, expenses, recurring cancellation and savings goals.

## Environment and evidence

Run `powershell -NoProfile -File scripts/test-firebase.ps1` with one unlocked Android emulator. The script builds APKs, checks JVM tests, starts local Auth/Firestore services with the repository rules and runs Android instrumentation. Raw output: `scripts/temp/android-tests.log`.

Backend tests use the isolated `demo-ganudenu-qa` project, Auth port 9098 and Firestore port 8085. Device: Android emulator API 37. Each new finance test has a unique authenticated test account and fresh in-memory Room database.

Coverage levels: **UI integration** uses controlled/fake repositories; **backend integration** uses real Firebase SDKs against local emulators; **UI to storage** uses production screens, ViewModels, Room and Firebase repositories with local emulators. UI-to-storage tests start at a feature screen, rather than navigating from the app launcher. These must not be described as a complete production end-to-end run. In-memory Room does not prove persistence after process death.

Pass means the named assertions passed in this execution, not that the entire linked SRS requirement is complete. Fail means an assertion/test error occurred. Blocked means execution could not proceed. Not run means no execution evidence.

Fresh JVM execution: **37 tests passed, 0 failures, 0 errors, 0 skipped**. Forced with `:app:cleanTestDebugUnitTest :app:testDebugUnitTest`; XML evidence is under `app/build/test-results/testDebugUnitTest`. Includes 4 auth validation, 10 auth ViewModel, 7 income calculation, 10 income ViewModel, 3 expense entity checks, 2 goal formula checks and 1 example test. Formula/entity tests are supporting checks, not end-to-end workflow evidence.

## Test cases

| ID | Requirements | Steps | Expected result | Evidence and level | Status |
|---|---|---|---|---|---|
| TC-AUTH-01 | FR-AUTH-001, FR-AUTH-004 | Open signup; enter profile and credentials; submit; open Income; save AdSense LKR income; sign out through Settings. | Finance navigation opens; income belongs to test identity; logout removes finance screen. | UI integration: `AuthIncomeIntegrationTest.signupIncomeAndLogoutUseTheSameUserSession` | Pass |
| TC-AUTH-02 | FR-AUTH-002 | Open login for existing controlled account; enter credentials; submit. | Authenticated finance navigation appears for expected identity. | UI integration: `AuthIncomeIntegrationTest.existingAccountLoginOpensFinanceNavigation` | Pass |
| TC-AUTH-03 | FR-AUTH-001 | Register unique emulator account; create/edit profile; reload. | Account/profile persist with valid timestamps and unchanged creation time. | Backend: `FirebaseRepositoryIntegrationTest.signupAndProfileEditPersistValidTimestampsAndPreserveCreatedAt` | Pass |
| TC-AUTH-04 | FR-AUTH-002, NFR-REL-001 | Attempt duplicate signup, wrong-password login and disabled-account login. | Backend rejects each operation; separate UI checks display actionable errors. | Backend: `duplicateEmailWrongPasswordAndDisabledAccountAreRejectedByAuth`; UI: `AuthErrorUiTest` | Pass |
| TC-AUTH-05 | Implemented password reset feature | Submit invalid then valid reset email; obtain emulator reset code; change password; reuse code. | UI validates email; password changes; reused code rejected. Real email delivery excluded. | UI: reset dialog test; backend: `passwordResetActionCodeChangesThePasswordAndIsSingleUse` | Pass |
| TC-AUTH-06 | FR-AUTH-003, NFR-SEC-002 | Save user A income; authenticate B; attempt A's read/write; submit malformed income. | Rules deny cross-account access and malformed income. Other collections not covered by this test. | Backend: `rulesRejectMalformedIncomeAndCrossAccountReadsAndWrites` | Pass |
| TC-INC-01 | FR-INC-001, FR-INC-004 | Enter amount; change source cards; save selected source with frequency. | Inputs retained; correct source, LKR amount and frequency saved. | UI: `AddIncomeTest.sourceCardsRetainInputsAndSaveTheSelectedSource`; supporting ViewModel tests cover all sources | Pass |
| TC-INC-02 | FR-INC-003, FR-INC-009 | Open income overview with controlled confirmed/estimated records across sources. | Separate correct totals and all four source cards render. | UI: `IncomeDashboardTest.referenceDashboardRendersTotalsAndAllSourceCards` | Pass |
| TC-INC-03 | FR-INC-009 | Select another month in income overview. | Selected month and associated records update. | UI: `IncomeDashboardTest.monthPickerShowsOctoberWhenSelected`; supporting monthly ViewModel checks | Pass |
| TC-INC-04 | FR-INC-009, NFR-REL-001 | Open overview without records. | Zero/empty state; Log Income remains available. | UI: `IncomeDashboardTest.newAccountShowsZeroAndLogIncomeRemainsAvailable` | Pass |
| TC-INC-05 | FR-INC-001 | Open Log Income; enter valid income; save. | Separate add route opens; successful save returns to overview. | UI/navigation: `IncomeNavigationTest.logIncomeOpensSeparateRouteAndSaveReturnsToOverview` | Pass |
| TC-INC-06 | FR-INC-002 | Select historical date with date picker; save income. | Saved entry uses selected payment date. | UI: `IncomeRecoveryUiTest.calendarSelectionIsSavedAsTheChosenPaymentDate` | Pass |
| TC-INC-07 | NFR-REL-002 | Simulate denied save; retain draft; remove failure; retry. | Error visible; retry uses same draft ID. | UI: `IncomeRecoveryUiTest.permissionDeniedKeepsTheDraftAndRetryUsesTheSameId` | Pass |
| TC-INC-08 | NFR-REL-002 | Fill draft/select month; parcel saved state; recreate ViewModel. | Fields, month and retry identity restored. Does not terminate Android process. | UI/state: `IncomeRecoveryUiTest.processStateParcelRestoresMonthFieldsAndRetryIdentity` | Pass |
| TC-INC-09 | FR-INC-009 | Write December/January entries; query each month; retry same ID. | Correct year boundaries; retry does not duplicate record. | Backend: `incomeQueriesRespectYearBoundaryAndRetriesOverwriteTheSameDocument` | Pass |
| TC-INC-10 | FR-OFF-001, FR-OFF-003, NFR-REL-002 | Cache income; disable Firestore networking; save pending entry; reconnect. | Cache/pending metadata observed; exactly one income synchronizes. | Backend: `offlineCachePendingWriteAndReconnectSyncKeepOneRecord` | Pass |
| TC-EXP-01 | FR-EXP-001, FR-EXP-002, FR-EXP-003, FR-EXP-004, FR-EXP-006, FR-EXP-007 | Open expense form; enter 1500; choose UberEats and Card; save. | Correct Room row becomes SYNCED; matching cloud document contains amount/payment method. | UI to storage: `FinanceWorkflowIntegrationTest.expenseFormPersistsSelectedCategoryAndPaymentToRoomAndCloud` | Pass |
| TC-EXP-02 | FR-OFF-001, FR-OFF-003, NFR-REL-002 | Disable repository network; save 900 in expense form; inspect pending row; reconnect. | PENDING row survives; same ID becomes SYNCED; exactly one cloud expense. No process restart/independent retry worker tested. | UI to storage: `offlineExpenseRemainsPendingThenSyncsOnReconnect` | Pass |
| TC-REC-01 | Cancellation portion of FR-REC-004 | Seed active gym template and linked expense; open recurring screen; tap Deactivate. | Template inactive in Room/cloud; historical expense remains. Full template editing excluded. | UI to storage: `recurringCancellationUpdatesCloudWithoutDeletingExpenseHistory` | Pass |
| TC-GOAL-01 | FR-GOAL-001, FR-GOAL-002 | Open goal form; enter MacBook Pro M4, target 490000, saved 11200, date November 05, 2027; save. | Success feedback; matching fields stored in Room/cloud. | UI to storage: `goalFormPersistsBaselineToRoomAndCloud` | Pass |
| TC-GOAL-02 | FR-GOAL-003, FR-GOAL-004; manual portion of FR-GOAL-005 | Seed goal 490000/11200 with 365 days remaining; open goal screen; enter hypothetical expense 15000. | Progress 2.3%; current 30-day-month formula shows 11-day delay. Automatic expense-save feedback excluded. | UI to storage: `savedGoalDisplaysProgressAndImpulseDelay` | Pass |

## Final execution result

Final complete Android run: **33 passed, 0 failures, 0 skipped**, elapsed instrumentation time **140.186 seconds**, runner result `OK (33 tests)`, script exit code 0. Together with the fresh 37 JVM tests, **70 automated tests passed**. All 21 case specifications above have passing evidence; some cases aggregate several assertions/tests. Eight separate acceptance checks remain Not run.

New UI-to-storage checks passed: expense save, offline expense/reconnect, recurring cancellation preserving history, goal form persistence, and goal progress/impulse delay. No production application source was changed. The runner now accepts optional `-TestClass` for focused reruns.

Earlier runs exposed test harness issues (JUnit return types and an invalid scroll action on a non-scrollable expense screen). These were corrected before the final complete rerun. Earlier incomplete/failed runs are not counted as final passes.

### Final Android method results

| Test class | Method | Status |
|---|---|---|
| AddIncomeTest | sourceCardsRetainInputsAndSaveTheSelectedSource | Pass |
| AddIncomeTest | addIncomeMatchesCommonFormDesign | Pass |
| AuthErrorUiTest | wrongPasswordStaysOnLoginAndShowsAnError | Pass |
| AuthErrorUiTest | resetDialogRejectsInvalidEmailAndThenSendsToTheRequestedAddress | Pass |
| AuthErrorUiTest | duplicateSignupEmailKeepsTheFormAndOffersSignIn | Pass |
| AuthErrorUiTest | profileReadErrorRetriesWithoutOpeningProfileCompletion | Pass |
| AuthErrorUiTest | disabledUserGetsAnActionableError | Pass |
| AuthIncomeIntegrationTest | existingAccountLoginOpensFinanceNavigation | Pass |
| AuthIncomeIntegrationTest | signupIncomeAndLogoutUseTheSameUserSession | Pass |
| AuthIncomeVisualTest | signupLayoutMatchesBaseline | Pass |
| AuthIncomeVisualTest | addIncomeLayoutMatchesBaseline | Pass |
| AuthIncomeVisualTest | incomeDashboardLayoutMatchesBaseline | Pass |
| AuthIncomeVisualTest | loginLayoutMatchesBaseline | Pass |
| ExampleInstrumentedTest | useAppContext | Pass |
| FinanceWorkflowIntegrationTest | goalFormPersistsBaselineToRoomAndCloud | Pass |
| FinanceWorkflowIntegrationTest | recurringCancellationUpdatesCloudWithoutDeletingExpenseHistory | Pass |
| FinanceWorkflowIntegrationTest | savedGoalDisplaysProgressAndImpulseDelay | Pass |
| FinanceWorkflowIntegrationTest | offlineExpenseRemainsPendingThenSyncsOnReconnect | Pass |
| FinanceWorkflowIntegrationTest | expenseFormPersistsSelectedCategoryAndPaymentToRoomAndCloud | Pass |
| FirebaseRepositoryIntegrationTest | passwordResetActionCodeChangesThePasswordAndIsSingleUse | Pass |
| FirebaseRepositoryIntegrationTest | incomeQueriesRespectYearBoundaryAndRetriesOverwriteTheSameDocument | Pass |
| FirebaseRepositoryIntegrationTest | signupAndProfileEditPersistValidTimestampsAndPreserveCreatedAt | Pass |
| FirebaseRepositoryIntegrationTest | rulesRejectMalformedIncomeAndCrossAccountReadsAndWrites | Pass |
| FirebaseRepositoryIntegrationTest | offlineCachePendingWriteAndReconnectSyncKeepOneRecord | Pass |
| FirebaseRepositoryIntegrationTest | duplicateEmailWrongPasswordAndDisabledAccountAreRejectedByAuth | Pass |
| IncomeDashboardTest | monthPickerShowsOctoberWhenSelected | Pass |
| IncomeDashboardTest | newAccountShowsZeroAndLogIncomeRemainsAvailable | Pass |
| IncomeDashboardTest | referenceDashboardRendersTotalsAndAllSourceCards | Pass |
| IncomeErrorTest | backendErrorsDescribeAuthRulesAndConnectionSeparately | Pass |
| IncomeNavigationTest | logIncomeOpensSeparateRouteAndSaveReturnsToOverview | Pass |
| IncomeRecoveryUiTest | calendarSelectionIsSavedAsTheChosenPaymentDate | Pass |
| IncomeRecoveryUiTest | processStateParcelRestoresMonthFieldsAndRetryIdentity | Pass |
| IncomeRecoveryUiTest | permissionDeniedKeepsTheDraftAndRetryUsesTheSameId | Pass |
