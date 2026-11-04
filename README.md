# Ganu Denu (ගනුදෙනු) - Personal Finance Management System

## 1. Project Overview
An advanced Android personal finance management system designed for multi-source, multi-currency income tracking, frictionless daily and recurring expense management, and real-time financial goal forecasting. Built for SE3092 Assignment 01.

## 2. Architectural & Technical Summary
- **UI & Design:** Jetpack Compose with Material Design 3, custom navigation graphs, and adaptive IME layout padding.
- **Architecture:** Strict MVVM (Model-View-ViewModel) pattern with reactive Unidirectional Data Flow (UDF).
- **Asynchrony:** Kotlin Coroutines and lifecycle-aware `StateFlow`.
- **Dependency Injection (Hybrid Setup):** 
  - **Hilt:** Manages ViewModels, Repositories, Room Database daos, and Settings/Goals modules.
  - **Koin:** Manages Auth and Income tracking modules.
- **Persistence & Cloud Sync:** 
  - **Room Database (v3):** Local-first offline persistence supporting `ExpenseEntity`, `RecurringItemEntity`, and `GoalEntity`.
  - **Firebase Authentication & Firestore:** Cloud-backed user profiles, income ledgers, and secure ownership rules (`isOwner`).

## 3. Core Features
- **Secure Authentication:** Email/password login and account registration.
- **Dynamic Goal Engine:** Real-time tracking for major targets (e.g., MacBook Pro M4 acquisition), interactive progress rings, and an **Impulse Checker** to simulate financial trade-offs.
- **Income & Expense Hub:** Track confirmed vs. estimated cash flows, daily expenses, and active recurring subscriptions.
- **Profile & Settings Manager:** Live cloud-synced user profiles (Name, Phone, Email, Currency) and account security controls.

## 4. Firebase Configuration Steps
See [Firebase setup guide](docs/FIREBASE_SETUP.md) for console steps, custom security rules (`isOwner`, `validProfile`, `validIncome`), schema design, and verification.

> ⚠️ **Important:** Ensure your group's real `app/google-services.json` is placed in the `app/` directory and Firestore security rules are published before building. See the [authentication guide](docs/AUTHENTICATION.md) and [income implementation docs](docs/INCOME.md).

## 5. Build & Execution Instructions
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34+
- **IDE:** Android Studio (Koala / Ladybug or newer recommended with Kotlin Multiplatform/Compose compiler support).
- **Setup:** Clone the repository, sync Gradle with dependencies, ensure `google-services.json` is present, and run on an API 34+ emulator or physical device.

## 6. Team Contributions & Navigation Flow
- Authentication and Income Hub modules managed via Koin and Firestore.
- Dashboard, Goal Engine, Room persistence, and Settings/Profile synchronization managed via Hilt and Jetpack Compose.
