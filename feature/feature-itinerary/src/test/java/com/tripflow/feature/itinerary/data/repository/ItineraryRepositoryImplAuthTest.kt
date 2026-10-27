package com.tripflow.feature.itinerary.data.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.data.remote.ItineraryApi
import com.tripflow.feature.itinerary.data.remote.dto.ItineraryResponseDto
import com.tripflow.feature.itinerary.session.TokenRefresher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.Proxy

// Token scaduto: alla prima 401 il repository rinnova il token e ripete la chiamata una volta.
class ItineraryRepositoryImplAuthTest {

    // Delegato per i metodi non usati dai test: se venissero chiamati il test fallisce.
    private val unused = Proxy.newProxyInstance(
        ItineraryApi::class.java.classLoader,
        arrayOf(ItineraryApi::class.java),
    ) { _, method, _ -> throw UnsupportedOperationException(method.name) } as ItineraryApi

    private fun httpError(code: Int) = HttpException(
        Response.error<Unit>(code, "{}".toResponseBody("application/json".toMediaType()))
    )

    private class FakeRefresher(private val succeeds: Boolean) : TokenRefresher {
        var refreshCalls = 0
        override fun currentToken(): String = "token-scaduto"
        override suspend fun refreshAfter(failedToken: String?): Boolean {
            refreshCalls++
            return succeeds
        }
    }

    private fun apiFailingFirstWith(code: Int, counter: IntArray) = object : ItineraryApi by unused {
        override suspend fun getMyItineraries(): List<ItineraryResponseDto> {
            counter[0]++
            if (counter[0] == 1) throw httpError(code)
            return emptyList()
        }
    }

    @Test
    fun `401 refreshes the token and retries once`() = runTest {
        val calls = IntArray(1)
        val refresher = FakeRefresher(succeeds = true)

        val result = ItineraryRepositoryImpl(apiFailingFirstWith(401, calls), tokenRefresher = refresher)
            .getMyItineraries()

        assertTrue(result is UiState.Success)
        assertEquals(2, calls[0])
        assertEquals(1, refresher.refreshCalls)
    }

    @Test
    fun `401 with rejected refresh returns error without retry`() = runTest {
        val calls = IntArray(1)
        val refresher = FakeRefresher(succeeds = false)

        val result = ItineraryRepositoryImpl(apiFailingFirstWith(401, calls), tokenRefresher = refresher)
            .getMyItineraries()

        assertTrue(result is UiState.Error)
        assertEquals(1, calls[0])
    }

    @Test
    fun `other http errors are not retried`() = runTest {
        val calls = IntArray(1)
        val refresher = FakeRefresher(succeeds = true)

        val result = ItineraryRepositoryImpl(apiFailingFirstWith(500, calls), tokenRefresher = refresher)
            .getMyItineraries()

        assertTrue(result is UiState.Error)
        assertEquals(1, calls[0])
        assertEquals(0, refresher.refreshCalls)
    }
}
