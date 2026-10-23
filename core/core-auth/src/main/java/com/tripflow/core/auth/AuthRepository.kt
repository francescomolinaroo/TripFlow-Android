package com.tripflow.core.auth

import android.content.Context
import android.net.Uri
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.auth.ChangePasswordRequest
import com.tripflow.core.network.auth.ForgotPasswordRequest
import com.tripflow.core.network.auth.LoginRequest
import com.tripflow.core.network.auth.LogoutRequest
import com.tripflow.core.network.auth.PublicUserResponse
import com.tripflow.core.network.auth.RefreshTokenRequest
import com.tripflow.core.network.auth.RegisterRequest
import com.tripflow.core.network.auth.RegisterResponse
import com.tripflow.core.network.auth.ResendVerificationRequest
import com.tripflow.core.network.auth.ResetPasswordRequest
import com.tripflow.core.network.auth.UpdateProfileRequest
import com.tripflow.core.network.auth.VerifyEmailRequest
import com.tripflow.core.network.auth.UserLookupByKeycloakIdsRequest
import com.tripflow.core.network.auth.UserLookupRequest
import com.tripflow.core.network.auth.UserResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

open class AuthRepository(context: Context? = null) {
    private val tokenStorage = context?.let { TokenStorage(it) }

    open suspend fun checkSession(): Result<UserResponse> {
        if (tokenStorage?.getAccessToken() == null) {
            return Result.failure(IllegalStateException("Sessione non disponibile"))
        }
        return getMe()
    }

    open suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            Result.success(ApiClient.authApi.register(request))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    open suspend fun login(email: String, password: String): Result<UserResponse> {
        return try {
            val response = ApiClient.authApi.login(
                LoginRequest(email = email, password = password)
            )
            tokenStorage?.save(response.accessToken, response.refreshToken)
            val user = response.user
            if (user != null) Result.success(user) else getMe()
        } catch (exception: HttpException) {
            // 403 = email non verificata: la UI deve mandare l'utente alla verifica, non trattarlo
            // come credenziali errate
            if (exception.code() == 403) {
                Result.failure(EmailNotVerifiedException(email))
            } else {
                Result.failure(exception)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    open suspend fun getMe(): Result<UserResponse> =
        withAuth { token -> ApiClient.authApi.getMe(token) }

    open suspend fun updateProfile(request: UpdateProfileRequest): Result<UserResponse> =
        withAuth { token -> ApiClient.authApi.updateProfile(token, request) }

    open suspend fun uploadAvatar(uri: Uri, context: Context): Result<String> {
        return withAuth { token ->
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Impossibile leggere il file immagine")

            val requestBody = RequestBody.create(MediaType.parse(mimeType), bytes)
            val part = MultipartBody.Part.createFormData("file", "avatar.$extension", requestBody)
            val response = ApiClient.authApi.uploadAvatar(token, part)
            response["url"] ?: throw IllegalStateException("URL avatar non restituito dal server")
        }
    }

    open suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        withAuth { token ->
            ApiClient.authApi.changePassword(token, ChangePasswordRequest(currentPassword, newPassword))
        }

    open suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            ApiClient.authApi.forgotPassword(ForgotPasswordRequest(email = email))
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    open suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        return try {
            ApiClient.authApi.resetPassword(
                ResetPasswordRequest(email = email, code = code, newPassword = newPassword)
            )
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    open suspend fun verifyEmail(email: String, code: String): Result<Unit> {
        return try {
            ApiClient.authApi.verifyEmail(VerifyEmailRequest(email = email, code = code))
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    open suspend fun resendVerification(email: String): Result<Unit> {
        return try {
            ApiClient.authApi.resendVerification(ResendVerificationRequest(email = email))
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    open suspend fun deleteAccount(): Result<Unit> {
        return withAuth { token ->
            ApiClient.authApi.deleteAccount(token)
            logout()
        }
    }

    suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = tokenStorage?.getRefreshToken() ?: return false
            val response = ApiClient.authApi.refresh(
                RefreshTokenRequest(refreshToken = refreshToken)
            )
            tokenStorage.save(response.accessToken, response.refreshToken)
            true
        } catch (exception: Exception) {
            false
        }
    }

    open suspend fun logout() {
        val refreshToken = tokenStorage?.getRefreshToken()
        tokenStorage?.clear()
        if (!refreshToken.isNullOrBlank()) {
            try {
                ApiClient.authApi.logout(LogoutRequest(refreshToken))
            } catch (_: Exception) {
                // Revoca best-effort
            }
        }
    }

    suspend fun getPublicUserByKeycloakId(keycloakId: String): Result<PublicUserResponse> {
        return try {
            Result.success(ApiClient.authApi.getPublicUserByKeycloakId(keycloakId))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getPublicUserById(id: String): Result<PublicUserResponse> {
        return try {
            Result.success(ApiClient.authApi.getPublicUserById(id))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun lookupUsers(emails: List<String>): Result<List<PublicUserResponse>> =
        withAuth { token ->
            ApiClient.authApi.lookupUsers(token, UserLookupRequest(emails))
        }

    suspend fun lookupUsersByKeycloakIds(keycloakIds: List<String>): Result<List<PublicUserResponse>> =
        withAuth { token ->
            ApiClient.authApi.lookupUsersByKeycloakIds(token, UserLookupByKeycloakIdsRequest(keycloakIds))
        }

    // Esegue il blocco con il token corrente; in caso di 401 tenta il refresh e riprova una volta.
    private suspend fun <T> withAuth(block: suspend (bearerToken: String) -> T): Result<T> {
        val token = tokenStorage?.getAccessToken()
            ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
        return try {
            Result.success(block("Bearer $token"))
        } catch (exception: HttpException) {
            if (exception.response()?.code() == 401 && refreshToken()) {
                val refreshed = tokenStorage.getAccessToken()
                    ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
                try {
                    Result.success(block("Bearer $refreshed"))
                } catch (retryException: Exception) {
                    logout()
                    Result.failure(retryException)
                }
            } else {
                if (exception.response()?.code() == 401) logout()
                Result.failure(exception)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
