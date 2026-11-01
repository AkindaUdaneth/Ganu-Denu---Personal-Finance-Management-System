package com.sliit.se3092.GanuDenu.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.income.FirestoreIncomeRepository
import com.sliit.se3092.GanuDenu.data.income.IncomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IncomeModule {
    @Provides
    @Singleton
    fun provideIncomeRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): IncomeRepository {
        return FirestoreIncomeRepository(auth, firestore)
    }
}