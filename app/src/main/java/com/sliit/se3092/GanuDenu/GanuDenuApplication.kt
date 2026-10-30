package com.sliit.se3092.GanuDenu

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import com.sliit.se3092.GanuDenu.data.auth.BiometricPreferences
import com.sliit.se3092.GanuDenu.data.auth.DeviceBiometricPreferences
import com.sliit.se3092.GanuDenu.data.auth.FirebaseAuthRepository
import com.sliit.se3092.GanuDenu.ui.auth.AuthViewModel
import com.sliit.se3092.GanuDenu.ui.settings.SettingsViewModel
import com.sliit.se3092.GanuDenu.data.income.IncomeRepository
import com.sliit.se3092.GanuDenu.data.income.FirestoreIncomeRepository
import com.sliit.se3092.GanuDenu.ui.income.IncomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class GanuDenuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GanuDenuApplication)
            modules(module {
                single { FirebaseAuth.getInstance() }
                single { FirebaseFirestore.getInstance() }
                single<AuthRepository> { FirebaseAuthRepository(get(), get()) }
                single<IncomeRepository> { FirestoreIncomeRepository(get(), get()) }
                single<BiometricPreferences> { DeviceBiometricPreferences(androidContext()) }
                viewModel { AuthViewModel(get(), get()) }
                viewModel { SettingsViewModel(get()) }
                viewModel { IncomeViewModel(get()) }
            })
        }
    }
}
