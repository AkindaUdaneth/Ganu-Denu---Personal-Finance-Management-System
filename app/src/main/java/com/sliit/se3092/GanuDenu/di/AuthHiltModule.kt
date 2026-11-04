package com.sliit.se3092.GanuDenu.di

import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import com.sliit.se3092.GanuDenu.data.auth.FirebaseAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthHiltModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository
}
