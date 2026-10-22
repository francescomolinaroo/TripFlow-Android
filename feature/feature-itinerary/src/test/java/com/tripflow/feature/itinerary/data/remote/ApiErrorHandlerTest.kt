package com.tripflow.feature.itinerary.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiErrorHandlerTest {

    @Test
    fun `uses ServiceError message for conflict responses`() {
        val body = """
            {"timestamp":null,"path":"/api/itineraries/1/shares","message":"Itinerary already shared with this user"}
        """.trimIndent()
        val response = Response.error<Unit>(
            409,
            body.toResponseBody("application/json".toMediaType())
        )

        val error = ApiErrorHandler.from(HttpException(response))

        assertEquals("Itinerary already shared with this user", error.message)
        assertFalse(error.retryable)
    }

    @Test
    fun `falls back to a readable message when the body is empty`() {
        val response = Response.error<Unit>(
            403,
            "".toResponseBody("application/json".toMediaType())
        )

        val error = ApiErrorHandler.from(HttpException(response))

        assertEquals("Non hai i permessi per questa operazione", error.message)
        assertFalse(error.retryable)
    }

    @Test
    fun `maps validation and not found fallbacks`() {
        val validationError = ApiErrorHandler.from(
            HttpException(Response.error<Unit>(400, "".toResponseBody()))
        )
        val notFoundError = ApiErrorHandler.from(
            HttpException(Response.error<Unit>(404, "".toResponseBody()))
        )

        assertEquals("Richiesta non valida: controlla i dati inseriti", validationError.message)
        assertEquals("Risorsa non trovata", notFoundError.message)
        assertFalse(validationError.retryable)
        assertFalse(notFoundError.retryable)
    }

    @Test
    fun `maps the expected conflict codes as non retryable`() {
        val forbidden = ApiErrorHandler.from(
            HttpException(Response.error<Unit>(403, "".toResponseBody()))
        )
        val conflict = ApiErrorHandler.from(
            HttpException(Response.error<Unit>(409, "".toResponseBody()))
        )

        assertFalse(forbidden.retryable)
        assertFalse(conflict.retryable)
        assertEquals("Conflitto: la risorsa esiste già", conflict.message)
    }

    @Test
    fun `marks server and network errors as retryable`() {
        val serverError = ApiErrorHandler.from(
            HttpException(Response.error<Unit>(503, "".toResponseBody()))
        )
        val networkError = ApiErrorHandler.from(IOException("timeout"))

        assertTrue(serverError.retryable)
        assertTrue(networkError.retryable)
    }
}
