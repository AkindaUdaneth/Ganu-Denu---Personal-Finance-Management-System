# Login and sign-up

Implemented on `feature/auth-login-signup` using the supplied black/yellow mockups.

Integrated with `feature/income-dashboard`: GanuDenuApplication provides a single Koin setup for auth, income and settings; GanuDenuApp guards all finance navigation. The previous IncomeApplication bootstrap is replaced. AuthIncomeIntegrationTest verifies login plus signup/profile -> income save under the same UID -> Settings/logout using fake repositories, without creating live Firebase accounts.

## Architecture and data

`AuthScreen -> AuthViewModel -> AuthRepository -> Firebase Auth / Firestore`

`AuthScreen` hosts session/biometric states and selects `LoginScreen` or `SignUpScreen`. Each form has its own file; reusable fields, buttons and biometric controls are in `AuthComponents.kt`. Profile completion belongs to the sign-up screen. The forms can be reviewed and committed separately after the shared foundation.

Koin wires the repository and ViewModels. Immutable StateFlow exposes validation, loading, error and session states. UI contains form rendering and Android biometric prompt presentation; Firebase calls stay in the repository. Finance navigation opens only after authentication and a confirmed saved profile. Settings loads the real name/email; Log Out clears the Firebase session and removes finance navigation.

Firebase Auth stores the email/password identity. Firestore `users/{uid}` stores only:

| Field | Type / meaning |
| --- | --- |
| name | trimmed string, 1-80 characters |
| phone | normalized string, optional leading + and 9-15 digits |
| email | authenticated Firebase account email |
| preferredCurrency | LKR |
| createdAt | server timestamp |

Phone is contact information, not SMS-verified authentication. Passwords are never written to Firestore or app preferences. Password reset sends Firebase's reset email and displays a generic confirmation.

Registration and profile creation are separate backend operations. If profile creation fails after Auth succeeds, the account remains signed in and Complete your profile offers a retry. A transaction only creates a missing profile so retries do not duplicate accounts or overwrite an existing profile. Initial profile reads require server confirmation; connection failures show Retry and Sign Out. Google Sign-In is not implemented.

## Biometrics

The switch selects optional device unlock after email/password authentication. Android's strong biometric prompt must succeed before the UID-scoped preference is enabled. Unsupported/unenrolled devices disable the switch. Restored opted-in sessions and returning from the background require unlock. Password fallback signs out of the locked session and allows email/password login. Cancelling does not open the dashboard; initial enrollment can be skipped.

This is a local app lock over a Firebase session, not a separate Firebase identity provider or cryptographic encryption. No credentials, biometric templates or tokens are stored by this feature. Device preferences are excluded from backup and device transfer.

## Publish rules before testing

Publish the updated `firestore.rules` in Firebase Console > Firestore Database > Rules. Profile writes validate allowed/required fields, name/phone format, account email, LKR currency and server-created immutable creation timestamps. Finance subcollections retain owner checks; their field validation is future work.

Or use an authenticated Firebase CLI:

```powershell
firebase deploy --only firestore:rules --project YOUR_REAL_PROJECT_ID
```

Local rule changes do not automatically publish to Firebase.

## Verification

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Unit tests use fakes for validation, normalized profile creation, profile-save retries without duplicate registration, biometric gating/opt-in, restore retry and password reset.

Device/live checklist:

1. Launch signed out; switch between login/signup and test blank/invalid fields.
2. Register an email you control. Confirm matching UIDs in Firebase Authentication and `users/{uid}`, with the six fields above and no password.
3. Settings must display your name/email. Log out and sign in again. Back must not expose another user's finance navigation.
4. Test wrong password, duplicate email and disconnected network: show errors/retry without a crash or blank screen.
5. Request a password reset and complete the emailed Firebase flow.
6. Interrupt profile saving after Auth creation; reconnect and retry Complete your profile. Confirm only one account/profile.
7. Enable/confirm biometrics, background/reopen and restart. Test cancellation, password fallback, rotation and a device without enrolled biometrics.
8. Rules Playground: deny unauthenticated/other-UID access; allow valid own profile creation; deny extra password fields, malformed phone, wrong email, invalid timestamp and changing createdAt.

Existing finance screens, Edit Profile and Delete Account are separate work; their sample data/placeholders are not replaced here.
