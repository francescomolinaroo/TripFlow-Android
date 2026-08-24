package com.tripflow.core.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenStorageInstrumentedTest {
    @Test
    fun saveReadAndClearTokens() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = TokenStorage(context)
        storage.clear()

        storage.save("access-token", "refresh-token")
        assertEquals("access-token", storage.getAccessToken())
        assertEquals("refresh-token", storage.getRefreshToken())

        storage.clear()
        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
    }
}