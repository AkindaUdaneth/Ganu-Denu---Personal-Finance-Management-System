# Firebase setup

## Console configuration

1. Open https://console.firebase.google.com/ and create a project or select the group's existing project. Analytics is optional.
2. Register an Android app with package name **com.sliit.se3092.ganudenu**, matching `applicationId` in `app/build.gradle.kts`. The uppercase Kotlin source directory is not the application ID.
3. Download the real `google-services.json` into `app/google-services.json`, then sync Gradle in Android Studio. Do not use a service-account JSON or placeholder values.
4. Authentication > Sign-in method: enable **Email/Password**.
5. Firestore Database > Create database: create the default database in production mode and choose its location carefully.
6. Firestore > Rules: paste the repository's `firestore.rules` and publish.
7. Give teammates access through Firebase project permissions using their own Google accounts.

The Google Services plugin generates the resources that initialize the default Firebase app automatically.

If Firebase CLI is installed and authenticated, deploy rules and indexes with:

```powershell
firebase deploy --only firestore:rules,firestore:indexes --project YOUR_REAL_PROJECT_ID
```

## Proposed shared schema

Income is now implemented with the schema in [Income dashboard](INCOME.md#stored-schema), including decimal original amounts and `occurredAt`. Use that contract for income rather than the earlier proposal below.

Use the signed-in Firebase Authentication UID for all user paths.

| Path | Suggested fields |
| --- | --- |
| `users/{uid}` | name, phone, email, preferredCurrency, createdAt |
| `users/{uid}/incomes/{id}` | source, originalAmountMinor, currency, exchangeRateToLkr, amountLkrMinor, status, receivedAt, projectOrInvoice, createdAt |
| `users/{uid}/expenses/{id}` | amountLkrMinor, category, spendingType, paymentMethod, occurredAt, note, createdAt |
| `users/{uid}/recurringItems/{id}` | name, amountLkrMinor, category, paymentMethod, frequency, nextDueAt, active |
| `users/{uid}/goals/{id}` | name, targetAmountLkrMinor, savedAmountLkrMinor, targetDate, createdAt |

The user profile is implemented; finance fields remain a proposed contract for upcoming repositories. Use Firestore timestamps for dates and integer minor units for fiat money (LKR 100.00 = 10000). Preserve original currency and conversion rate at receipt time. Cryptocurrency quantities need an agreed decimal representation rather than fiat minor units. Preserve losses as signed income. Keep confirmed receipts and estimated income separate in totals. Never store passwords or biometric data in Firestore.

Rules restrict access to the owner of the profile and its four named subcollections. All other paths are denied. User profile fields/types are validated; finance field validation remains pending.

No composite indexes are needed for profile access by UID. Single-field indexes support date ordering. When finance queries combine category/status filters and date ordering, commit the composite indexes requested by Firestore.

## Remaining application integration

Login/signup, Auth repository, profile saving, password reset, session management, Koin, biometric unlock and Settings logout/profile display are implemented. See [authentication guide](AUTHENTICATION.md).

- Add user-scoped Firestore repositories and connect the existing screens through ViewModels.
- Observe dashboard data with snapshot listeners; remove listeners when observation ends or a user signs out.
- Android Firestore disk persistence is enabled by default. Expose cached/pending writes and network errors in UI state. Decide how Room cooperates with Firestore.
- Account deletion must also address subcollections; deleting the parent document or Auth user does not remove them.

## Verification

1. Add the real config, sync Gradle and run `./gradlew.bat :app:assembleDebug`.
2. Register a fresh account and check Authentication > Users.
3. Save data from the app and verify the UID-scoped document path.
4. In Rules Playground, test unauthenticated denial, owner access and denial for another UID, for both profiles and each subcollection.
5. After listeners are connected, test real-time dashboard updates and offline/reconnection behavior.

Live verification requires the group's real Firebase configuration and a device/emulator. Finance flows remain separate work.

References: https://firebase.google.com/docs/android/setup and https://firebase.google.com/docs/firestore/security/rules-conditions
