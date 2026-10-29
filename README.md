# Ganu Denu - Personal Finance Management System

## 1. Project Overview
An Android personal finance system designed for multi-source, multi-currency income and frictionless expense tracking. Built for SE3092 Assignment 01.

## 2. Architectural Summary
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** Strict MVVM (Model-View-ViewModel)
- **Asynchrony:** Kotlin Coroutines & StateFlow
- **Dependency Injection:** Hilt/Koin integration is pending.
- **Persistence:** Room scaffolding exists; Firebase Auth and Firestore SDKs are configured. Repositories and screen integration are pending.

## 3. Firebase Configuration Steps
See [Firebase setup guide](docs/FIREBASE_SETUP.md) for console steps, security rules, proposed schema, and verification.

Add your group's real `app/google-services.json` to connect to its Firebase project. Current screens still use sample data.

## 4. Build Instructions
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34+
- Ensure `google-services.json` is placed in the `app/` directory before syncing Gradle.

