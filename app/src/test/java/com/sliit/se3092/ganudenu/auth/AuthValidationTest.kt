package com.sliit.se3092.ganudenu.auth

import com.sliit.se3092.GanuDenu.ui.auth.AuthValidation
import org.junit.Assert.*
import org.junit.Test

class AuthValidationTest {
    @Test fun emailRejectsBlankMalformedAndWhitespace() {
        listOf("", "person", "person@", "a b@example.com", "a@@example.com").forEach {
            assertNotNull(AuthValidation.email(it))
        }
        assertNull(AuthValidation.email(" person@example.com "))
    }

    @Test fun phoneAcceptsLocalAndInternationalFormatting() {
        assertNull(AuthValidation.phone("+94 71 123 4567"))
        assertNull(AuthValidation.phone("071-123-4567"))
        assertEquals("+94711234567", AuthValidation.normalizePhone("+94 71 123 4567"))
        listOf("", "123", "++94711234567", "phone123456789").forEach { assertNotNull(AuthValidation.phone(it)) }
    }

    @Test fun signUpEnforcesMinimumButLoginDoesNotRejectExistingPasswords() {
        assertNotNull(AuthValidation.password("12345", true))
        assertNull(AuthValidation.password("123456", true))
        assertNull(AuthValidation.password("short", false))
        assertNotNull(AuthValidation.password("", false))
    }

    @Test fun nameRequiresNonBlankBoundedInput() {
        assertNotNull(AuthValidation.name("   "))
        assertNotNull(AuthValidation.name("a".repeat(81)))
        assertNull(AuthValidation.name("Kasun Perera"))
    }
}
