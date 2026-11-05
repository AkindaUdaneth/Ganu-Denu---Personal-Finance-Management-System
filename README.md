# Ganu Denu (ගනුදෙනු) - Personal Finance Management System

## 1. Project Overview
An advanced Android personal finance management system designed for multi-source, multi-currency income tracking, frictionless daily and recurring expense management, and real-time financial goal forecasting. Built for SE3092 Assignment 01 based on the financial reality and behavioural friction points of Mr. Kavindu Silva.

## 2. Architectural & Technical Summary
- **UI & Design:** Jetpack Compose with Material Design 3, custom navigation graphs, and adaptive IME layout padding.
- **Architecture:** Strict MVVM (Model-View-ViewModel) pattern with reactive Unidirectional Data Flow (UDF).
- **Asynchrony:** Kotlin Coroutines and lifecycle-aware `StateFlow`.
- **Dependency Injection (Hybrid Setup):** 
  - **Hilt:** Manages ViewModels, Repositories, Room Database DAOs, and Settings/Goals modules.
  - **Koin:** Manages Auth and Income tracking modules.
- **Persistence & Cloud Sync:** 
  - **Room Database (v3):** Local-first offline persistence supporting `ExpenseEntity`, `RecurringItemEntity`, and `GoalEntity`.
  - **Firebase Authentication & Firestore:** Cloud-backed user profiles, income ledgers, and secure ownership rules (`isOwner`).

## 3. Project Structure
```text
com.sliit.se3092.GanuDenu/
├── data/
│   ├── auth/          # FirebaseAuthRepository & AuthRepository
│   ├── local/         # Room Database, DAOs, and Entities (ExpenseEntity, RecurringItemEntity, GoalEntity)
│   ├── income/        # Income data structures & repository
│   └── repository/    # ExpenseRepository, GoalRepository, etc.
├── di/                # Hilt & Koin dependency injection modules
├── ui/
│   ├── auth/          # AuthScreen, LoginScreen, SignUpScreen, AuthViewModel
│   ├── dashboard/     # DashboardScreen, DashboardViewModel
│   ├── expenses/      # ExpensesHubScreen, DailyExpenseLoggerScreen, RecurringExpenseScreen, ExpenseViewModel, ExpenseReminderReceiver
│   ├── goals/         # GoalEngineScreen, GoalFormScreen, GoalViewModel
│   ├── income/        # IncomeDashboardScreen, IncomeViewModel
│   ├── settings/      # SettingsScreen, ProfileFormScreen, SettingsViewModel
│   └── theme/         # Color, Type, Theme (Material Design 3)
└── MainActivity.kt
```

## 4. Core Features
- **Secure Authentication:** 
  - Email/password login, account registration, and password reset.
  - **Google Sign-In & Sign-Up:** Integrated via Android Credential Manager and Firebase Auth (`default_web_client_id`), with validation ensuring users logging in with Google without an account are prompted to sign up first.
- **Dynamic Goal Engine:** Real-time tracking for major targets (e.g., MacBook Pro M4 acquisition), interactive progress rings, and an **Impulse Checker** to simulate financial trade-offs.
- **Income & Expense Hub:** 
  - Track confirmed vs. estimated cash flows, daily expenses, and active recurring subscriptions.
  - **Committed & Recurring Expenses:** Stable alphabetical sorting (`ORDER BY name ASC`) to prevent list jumping on open during background sync, grouped active and deactivated sections with toggle controls, dynamic current month display on overhead banners, and smart 9:00 AM `AlarmManager` reminder notifications (with automatic alarm cancellation upon deactivation).
  - **Daily Expense Logger & Validation:** Supports past/current backfilling while strictly preventing future-dated entries via `DatePickerDialog` `maxDate` constraints and view model validation.
- **Profile & Settings Manager:** Live cloud-synced user profiles (Name, Phone, Email, Currency) and account security controls.

## 5. Firebase Configuration Steps
See [Firebase setup guide](docs/FIREBASE_SETUP.md) for console steps, custom security rules (`isOwner`, `validProfile`, `validIncome`), schema design, and verification.

### Setting up Google Sign-In & Firebase:
1. Register your Android app in Firebase Console with package name:
   ```text
   com.sliit.se3092.ganudenu
   ```
2. Obtain your debug SHA-1 signing fingerprint via terminal:
   ```bash
   ./gradlew signingReport
   ```
3. Add the SHA-1 fingerprint in **Firebase Console $\rightarrow$ Project Settings $\rightarrow$ Your Apps**.
4. Download `google-services.json` and place it in the project's root `app/` folder (`app/google-services.json`).
5. Add your OAuth Web Client ID inside `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
   ```

> ⚠️ **Important:** Ensure your group's real `app/google-services.json` is placed in the `app/` directory and Firestore security rules are published before building. See the [authentication guide](docs/AUTHENTICATION.md) and [income implementation docs](docs/INCOME.md).

## 6. Build & Execution Instructions
### Prerequisites
- **Android Studio:** Ladybug (2024.2.1+) or Koala Feature Drop with Kotlin 2.0+ support.
- **Android SDK:**
  - `minSdkVersion`: 26 (Android 8.0 Oreo)
  - `targetSdkVersion`: 34+
  - `compileSdk`: 34+
- **JDK:** Version 17 or Version 21.

### Running the App
1. Clone the repository:
   ```bash
   git clone https://github.com/AkindaUdaneth/Ganu-Denu---Personal-Finance-Management-System.git
   ```
2. Open the project in Android Studio and let Gradle sync dependencies.
3. Verify that `app/google-services.json` is in place and `strings.xml` contains your `default_web_client_id`.
4. Select an Android emulator (API Level 26 or higher with Google Play Services) or connect a physical device via USB debugging.
5. Build and run via Android Studio (`Shift + F10`) or via CLI:
   ```bash
   ./gradlew installDebug
   ```

## 7. Team Contributions

- **K.H.G.A. Udaneth** (`IT23750210`) — App Branding & Splash Screen, Email/Password Authentication (Login & Account Registration screens), and Multi-Source Income Subsystem (Income Dashboard, Confirmed vs. Estimated Status, 4-Source Grid, and Add Income Flow).
- **M.M.H.M. Valantina** (`IT23749320`) — Complete Expense Management System (Expenses Hub, Committed/Recurring Subscriptions Manager, Daily Expense Logger & Validation, Custom Category Modal, Room/Firestore Persistence), `AlarmManager` 9:00 AM Scheduled Reminders, and Google Sign-In & Sign-Up Integration via Android Credential Manager.
- **T.C. Dahanayaka** (`IT23746978`) — Clarity Financial Dashboard (Committed vs. Discretionary Spending Visualizer), Spending Analytics (Donut Breakdown & Top Expenses), Dynamic Goal Engine (MacBook Pro M4 Progress Ring & Behavioral Impulse Checker), Add/Edit Goal Flow, and Settings & Edit Profile Management.

