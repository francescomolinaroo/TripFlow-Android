package com.tripflow.feature.auth

import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.RegisterRequest
import com.tripflow.core.network.auth.RegisterResponse
import com.tripflow.core.network.auth.UpdateProfileRequest
import com.tripflow.core.network.auth.UserResponse

class FakeAuthRepository : AuthRepository() {

    var loginResult: Result<UserResponse> = Result.failure(IllegalStateException("non impostato"))
    var registerResult: Result<RegisterResponse> = Result.failure(IllegalStateException("non impostato"))
    var forgotPasswordResult: Result<Unit> = Result.success(Unit)
    var getMeResult: Result<UserResponse> = Result.failure(IllegalStateException("non impostato"))
    var updateProfileResult: Result<UserResponse> = Result.failure(IllegalStateException("non impostato"))
    var changePasswordResult: Result<Unit> = Result.success(Unit)
    var deleteAccountResult: Result<Unit> = Result.success(Unit)
    var verifyEmailResult: Result<Unit> = Result.success(Unit)
    var resetPasswordResult: Result<Unit> = Result.success(Unit)
    var resendVerificationResult: Result<Unit> = Result.success(Unit)

    var loginCalledWith: Pair<String, String>? = null
    var registerCalledWith: RegisterRequest? = null
    var forgotPasswordCalledWith: String? = null
    var changePasswordCalledWith: String? = null
    var verifyEmailCalledWith: Pair<String, String>? = null
    var resetPasswordCalledWith: Triple<String, String, String>? = null
    var resendVerificationCalledWith: String? = null
    var logoutCalled = false
    var hasSessionResult = false

    override fun hasSession(): Boolean = hasSessionResult

    override suspend fun login(email: String, password: String): Result<UserResponse> {
        loginCalledWith = email to password
        return loginResult
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        registerCalledWith = request
        return registerResult
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        forgotPasswordCalledWith = email
        return forgotPasswordResult
    }

    override suspend fun getMe(): Result<UserResponse> = getMeResult

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserResponse> =
        updateProfileResult

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        changePasswordCalledWith = newPassword
        return changePasswordResult
    }

    override suspend fun deleteAccount(): Result<Unit> = deleteAccountResult

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> {
        verifyEmailCalledWith = email to code
        return verifyEmailResult
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        resetPasswordCalledWith = Triple(email, code, newPassword)
        return resetPasswordResult
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        resendVerificationCalledWith = email
        return resendVerificationResult
    }

    override suspend fun logout() {
        logoutCalled = true
    }
}

fun sampleUser(
    id: String = "1",
    firstName: String = "Mario",
    lastName: String = "Rossi",
    email: String = "mario@example.com",
    role: String = "TRAVELER",
    dateOfBirth: String? = null,
    phoneNumber: String? = null,
    profileImage: String? = null,
) = UserResponse(id, firstName, lastName, email, role, dateOfBirth, phoneNumber, profileImage)
