package com.tripflow.feature.auth

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

internal fun isEmailValid(email: String): Boolean =
    email.isNotBlank() && EMAIL_REGEX.matches(email)

internal fun isPasswordValidForLogin(password: String): Boolean =
    password.length >= 6

internal fun isPasswordValidForRegister(password: String): Boolean =
    password.length >= 8

internal fun isDateOfBirthValid(date: String): Boolean =
    date.isBlank() || Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""").matches(date)

internal fun isPhoneValid(phone: String): Boolean =
    phone.isBlank() || Regex("""^\+?[\d\s\-]{6,20}$""").matches(phone)
