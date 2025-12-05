package com.zybooks.inspobook.util

import org.junit.Assert.*
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `valid email and password pass validation`() {
        val isValid = LoginValidator.isValidCredentials(
            email = "test@test.com",
            password = "test123"
        )
        assertTrue("Valid credentials should pass validation", isValid)
    }

    @Test
    fun `empty email or password fails validation`() {
        assertFalse(LoginValidator.isValidCredentials("", "test123"))
        assertFalse(LoginValidator.isValidCredentials("test@test.com", ""))
    }

    @Test
    fun `bad email format fails validation`() {
        assertFalse(LoginValidator.isValidCredentials("not-an-email", "password"))
        assertFalse(LoginValidator.isValidCredentials("noatsign.com", "password"))
        assertFalse(LoginValidator.isValidCredentials("a@nodot", "password"))
    }

    @Test
    fun `short password fails validation`() {
        assertFalse(LoginValidator.isValidCredentials("test@test.com", "123"))
    }
}
