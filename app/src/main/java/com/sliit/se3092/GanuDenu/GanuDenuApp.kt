package com.sliit.se3092.GanuDenu

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import com.sliit.se3092.GanuDenu.data.auth.FirebaseAuthRepository
import com.sliit.se3092.GanuDenu.data.income.FirestoreIncomeRepository
import com.sliit.se3092.GanuDenu.data.income.IncomeRepository
import com.sliit.se3092.GanuDenu.ui.auth.AuthViewModel
import com.sliit.se3092.GanuDenu.ui.income.IncomeViewModel
import com.sliit.se3092.GanuDenu.ui.settings.SettingsViewModel
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

@HiltAndroidApp
class GanuDenuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GanuDenuApp)
            modules(module {
                single { FirebaseAuth.getInstance() }
                single { FirebaseFirestore.getInstance() }
                single<AuthRepository> { FirebaseAuthRepository(get(), get()) }
                single<IncomeRepository> { FirestoreIncomeRepository(get(), get()) }
                viewModel { AuthViewModel(get()) }
                viewModel { SettingsViewModel(get()) }
                viewModel { IncomeViewModel(get(), get()) }
            })
        }
    }
}
