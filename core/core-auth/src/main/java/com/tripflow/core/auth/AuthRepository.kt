package com.tripflow.core.auth

import android.content.Context
import android.net.Uri
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.auth.ChangePasswordRequest
import com.tripflow.core.network.auth.PublicUserResponse
import com.tripflow.core.network.auth.RegisterRequest
import com.tripflow.core.network.auth.RegisterResponse
import com.tripflow.core.network.auth.UpdateProfileRequest
import com.tripflow.core.network.auth.UserLookupRequest
import com.tripflow.core.network.auth.UserResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class AuthRepository(context: Context? = null) {
    private val tokenStorage = context?.let { TokenStorage(it) }

    suspend fun checkSession(): Result<UserResponse> {
        if (tokenStorage?.getAccessToken() == null) {
            return Result.failure(IllegalStateException("Sessione non disponibile"))
        }

        return getMe()
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            Result.success(ApiClient.authApi.register(request))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun login(email: String, password: String): Result<UserResponse> {
        return try {
            val token = ApiClient.keycloakApi.login(
                clientId = "tripflow-app",
                grantType = "password",
                username = email,
                password = password
            )
            tokenStorage?.save(token.accessToken, token.refreshToken)
            getMe()
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getMe(): Result<UserResponse> {
        return try {
            val token = tokenStorage?.getAccessToken()
                ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
            Result.success(ApiClient.authApi.getMe("Bearer $token"))
        } catch (exception: HttpException) {
            if (exception.response()?.code() == 401 && refreshToken()) {
                try {
                    val refreshedToken = tokenStorage?.getAccessToken()
                        ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
                    Result.success(ApiClient.authApi.getMe("Bearer $refreshedToken"))
                } catch (retryException: Exception) {
                    logout()
                    Result.failure(retryException)
                }
            } else {
                logout()
                Result.failure(exception)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserResponse> {
        return try {
            val token = tokenStorage?.getAccessToken()
                ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
            Result.success(ApiClient.authApi.updateProfile("Bearer $token", request))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun uploadAvatar(uri: Uri, context: Context): Result<String> {
        return try {
            val token = tokenStorage?.getAccessToken()
                ?: return Result.failure(IllegalStateException("Sessione non disponibile"))

            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(IllegalStateException("Impossibile leggere il file immagine"))

            val mediaType = MediaType.parse(mimeType)
            val requestBody = RequestBody.create(mediaType, bytes)
            val part = MultipartBody.Part.createFormData(
                "file",
                "avatar.$extension",
                requestBody
            )

            val response = ApiClient.authApi.uploadAvatar("Bearer $token", part)
            val url = response["url"]
                ?: return Result.failure(IllegalStateException("URL avatar non restituito dal server"))
            Result.success(url)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            val token = tokenStorage?.getAccessToken()
                ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
            ApiClient.authApi.changePassword("Bearer $token", ChangePasswordRequest(newPassword))
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val token = tokenStorage?.getAccessToken()
                ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
            ApiClient.authApi.deleteAccount("Bearer $token")
            tokenStorage.clear()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = tokenStorage?.getRefreshToken() ?: return false
            val token = ApiClient.keycloakApi.refresh(
                clientId = "tripflow-app",
                grantType = "refresh_token",
                refreshToken = refreshToken
            )
            tokenStorage.save(token.accessToken, token.refreshToken ?: refreshToken)
            true
        } catch (exception: Exception) {
            false
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

    suspend fun lookupUsers(emails: List<String>): Result<List<PublicUserResponse>> {
        return try {
            val token = tokenStorage?.getAccessToken()
                ?: return Result.failure(IllegalStateException("Sessione non disponibile"))
            Result.success(ApiClient.authApi.lookupUsers("Bearer $token", UserLookupRequest(emails)))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun logout() {
        tokenStorage?.clear()
    }
}

