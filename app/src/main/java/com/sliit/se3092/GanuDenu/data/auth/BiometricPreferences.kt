package com.sliit.se3092.GanuDenu.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface BiometricPreferences {
    fun isEnabled(uid: String): Boolean
    fun setEnabled(uid: String, enabled: Boolean)
}

/** Device-only opt-in; contains no credentials, tokens or biometric templates. */
class DeviceBiometricPreferences @Inject constructor(
    @ApplicationContext context: Context
) : BiometricPreferences {
    private val preferences = context.getSharedPreferences("biometric_unlock", Context.MODE_PRIVATE)
    override fun isEnabled(uid: String) = preferences.getBoolean(uid, false)
    override fun setEnabled(uid: String, enabled: Boolean) {
        preferences.edit().putBoolean(uid, enabled).apply()
    }
}