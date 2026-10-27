package com.tripflow.feature.auth

import java.time.LocalDate
import java.time.format.DateTimeParseException

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

internal fun isEmailValid(email: String): Boolean =
    email.isNotBlank() && EMAIL_REGEX.matches(email)

internal fun isPasswordValidForLogin(password: String): Boolean =
    password.length >= 6

internal fun isPasswordValidForRegister(password: String): Boolean =
    password.length >= 8

// Data reale (niente 31 febbraio) e non nel futuro; vuota = campo facoltativo.
internal fun isDateOfBirthValid(date: String): Boolean {
    if (date.isBlank()) return true
    if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(date)) return false
    return try {
        !LocalDate.parse(date).isAfter(LocalDate.now())
    } catch (_: DateTimeParseException) {
        false
    }
}

