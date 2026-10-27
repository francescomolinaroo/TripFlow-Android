package com.tripflow.feature.itinerary.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class JwtSubjectTest {

    private fun token(payloadJson: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"RS256"}""".toByteArray())
        val payload = encoder.encodeToString(payloadJson.toByteArray())
        return "$header.$payload.firma"
    }

    @Test
    fun `extracts sub claim from jwt payload`() {
        assertEquals(
            "70cd15d7-3a55-407c-9909-1f575e86cbfe",
            JwtSubject.extract(token("""{"sub":"70cd15d7-3a55-407c-9909-1f575e86cbfe","exp":1}"""))
        )
    }

    @Test
    fun `returns null for missing or malformed tokens`() {
        assertNull(JwtSubject.extract(null))
        assertNull(JwtSubject.extract("non-un-jwt"))
        assertNull(JwtSubject.extract("a.!!!.c"))
        assertNull(JwtSubject.extract(token("""{"exp":1}""")))
    }
}
