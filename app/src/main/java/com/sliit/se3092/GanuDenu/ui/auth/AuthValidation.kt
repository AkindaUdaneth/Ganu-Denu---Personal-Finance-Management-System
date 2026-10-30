package com.sliit.se3092.GanuDenu.ui.auth

object AuthValidation {
    fun email(value: String): String? = when {
        value.trim().isEmpty() -> "Enter your email address."
        value.trim().length > 254 || !value.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) -> "Enter a valid email address."
        else -> null
    }

    fun password(value: String, signingUp: Boolean): String? = when {
        value.isEmpty() -> "Enter your password."
        signingUp && value.length < 6 -> "Use at least 6 characters."
        else -> null
    }

    fun name(value: String): String? = when {
        value.trim().isEmpty() -> "Enter your name."
        value.trim().length > 80 -> "Use no more than 80 characters."
        else -> null
    }

    fun phone(value: String): String? {
        val normalized = normalizePhone(value)
        return if (normalized.matches(Regex("^\\+?[0-9]{9,15}$"))) null
        else "Enter a valid phone number, e.g. +94 71 123 4567."
    }

    fun normalizePhone(value: String) = value.trim().replace(Regex("[\\s()\\-]"), "")
}
