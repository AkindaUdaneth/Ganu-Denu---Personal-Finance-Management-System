# Ganu Denu - Personal Finance Management System

## 1. Project Overview
An Android personal finance system designed for multi-source, multi-currency income and frictionless expense tracking. Built for SE3092 Assignment 01.

## 2. Architectural Summary
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** Strict MVVM (Model-View-ViewModel)
- **Asynchrony:** Kotlin Coroutines & StateFlow
- **Dependency Injection:** One Koin setup provides Auth, Income, device biometric preferences and Settings dependencies.
- **Persistence:** Firebase Authentication manages accounts; Firestore stores profiles at `users/{uid}` and income at `users/{uid}/incomes`. Other finance screens still use sample data and Room scaffolding.

## 3. Firebase Configuration Steps
See [Firebase setup guide](docs/FIREBASE_SETUP.md) for console steps, security rules, proposed schema, and verification.

Add your group's real `app/google-services.json` to connect to its Firebase project. Login/signup and the Settings profile use real user data. See [authentication implementation and test guide](docs/AUTHENTICATION.md).

## 4. Build Instructions
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34+
- Ensure `google-services.json` is placed in the `app/` directory before syncing Gradle.

## 5. Income Dashboard

After login/signup, the Income tab displays real-time, user-scoped confirmed/estimated income and four overview cards. Log Income opens a separate Add Income page with common LKR amount/date/frequency/status fields for all four sources. Settings logout returns to login and disposes finance navigation. See [income implementation and verification](docs/INCOME.md). Publish the updated Firestore rules before testing income writes.

