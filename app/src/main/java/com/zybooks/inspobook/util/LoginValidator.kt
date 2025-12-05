package com.zybooks.inspobook.util

/**
 * Validator for login credentials before Firebase
 *
 * Returns true if the email and password look acceptable to submit
 */
object LoginValidator {
    fun isValidCredentials(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            return false
        }

        // email format rules
        val atIndex = trimmedEmail.indexOf('@')
        if (atIndex <= 0 || atIndex == trimmedEmail.lastIndex || !trimmedEmail.substring(atIndex).contains(".")) {
            return false
        }

        // password length rules
        if (trimmedPassword.length < 6) {
            return false
        }

        return true
    }
}
