package com.tripflow.core.network

import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkErrorParserTest {

    @Test
    fun parseServerMessage_extractsMessageFromJson() {
        val json = """
            {
                "timestamp": "2026-10-29T14:30:00",
                "status": 400,
                "error": "Bad Request",
                "message": "Il numero di posti dell'attività (10) non può superare i posti liberi del viaggio (4)",
                "path": "/catalog-service/api/v1/activities"
            }
        """.trimIndent()
        val response = Response.error<Unit>(400, ResponseBody.create(null, json))
        val exception = HttpException(response)

        val message = NetworkErrorParser.parseServerMessage(exception)
        assertEquals(
            "Il numero di posti dell'attività (10) non può superare i posti liberi del viaggio (4)",
            message
        )
    }

    @Test
    fun parseServerMessage_returnsNullForEmptyJson() {
        val response = Response.error<Unit>(400, ResponseBody.create(null, "{}"))
        val exception = HttpException(response)

        assertNull(NetworkErrorParser.parseServerMessage(exception))
    }

    @Test
    fun parseServerMessage_extractsPlainText() {
        val text = "Errore personalizzato dal server"
        val response = Response.error<Unit>(400, ResponseBody.create(null, text))
        val exception = HttpException(response)

        assertEquals("Errore personalizzato dal server", NetworkErrorParser.parseServerMessage(exception))
    }

    @Test
    fun getUserMessage_returnsServerMessageWhenPresentOn400() {
        val json = """{"message": "Il numero di posti dell'attività (10) non può superare i posti liberi del viaggio (4)"}"""
        val response = Response.error<Unit>(400, ResponseBody.create(null, json))
        val exception = HttpException(response)

        val userMessage = NetworkErrorParser.getUserMessage(exception)
        assertEquals(
            "Il numero di posti dell'attività (10) non può superare i posti liberi del viaggio (4)",
            userMessage
        )
    }

    @Test
    fun getUserMessage_returnsFallbackWhenNoServerMessageOn400() {
        val response = Response.error<Unit>(400, ResponseBody.create(null, "{}"))
        val exception = HttpException(response)

        val userMessage = NetworkErrorParser.getUserMessage(exception)
        assertEquals("Dati non validi: controlla i campi", userMessage)
    }

    @Test
    fun getUserMessage_handlesIOException() {
        val exception = IOException("Failed to connect")
        val userMessage = NetworkErrorParser.getUserMessage(exception)
        assertEquals("Connessione assente: controlla la rete e riprova", userMessage)
    }

    @Test
    fun getUserMessage_handles401() {
        val response = Response.error<Unit>(401, ResponseBody.create(null, ""))
        val exception = HttpException(response)

        val userMessage = NetworkErrorParser.getUserMessage(exception)
        assertEquals("Sessione scaduta, effettua di nuovo il login", userMessage)
    }
}
