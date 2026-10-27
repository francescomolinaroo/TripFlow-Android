package com.tripflow.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TelefonoTest {

    @Test
    fun `componiTelefono unisce prefisso e cifre in formato E164`() {
        assertEquals("+393331234567", componiTelefono("+39", "333 123-4567"))
        assertEquals("+41791234567", componiTelefono("+41", "791234567"))
    }

    @Test
    fun `componiTelefono senza numero restituisce null`() {
        assertNull(componiTelefono("+39", ""))
        assertNull(componiTelefono("+39", "  -  "))
    }

    @Test
    fun `numero valido tra 6 e 12 cifre, vuoto ammesso`() {
        assertTrue(isNumeroValido(""))
        assertTrue(isNumeroValido("123456"))
        assertTrue(isNumeroValido("123456789012"))
        assertFalse(isNumeroValido("12345"))
        assertFalse(isNumeroValido("1234567890123"))
    }

    @Test
    fun `dividiTelefono separa un numero E164`() {
        assertEquals("+39" to "3331234567", dividiTelefono("+393331234567"))
        assertEquals("+44" to "7911123456", dividiTelefono("+447911123456"))
        assertEquals("+1" to "2025550123", dividiTelefono("+12025550123"))
    }

    @Test
    fun `dividiTelefono preferisce il prefisso piu lungo`() {
        // +378 (San Marino) e non +3 seguito da altro
        assertEquals("+378" to "0549123456", dividiTelefono("+3780549123456"))
    }

    @Test
    fun `dividiTelefono gestisce i formati salvati prima del formato unico`() {
        assertEquals("+39" to "3331234567", dividiTelefono("3331234567"))
        assertEquals("+39" to "3331234567", dividiTelefono("+39 333 123 4567"))
        assertEquals("+39" to "3331234567", dividiTelefono("0039 333 1234567"))
        assertEquals("+39" to "3331234567", dividiTelefono("393331234567"))
    }

    @Test
    fun `dividiTelefono vuoto parte dal prefisso italiano`() {
        assertEquals(PREFISSO_PREDEFINITO to "", dividiTelefono(null))
        assertEquals(PREFISSO_PREDEFINITO to "", dividiTelefono(""))
    }

    @Test
    fun `prefisso fuori elenco ricomponendo da lo stesso numero`() {
        val (prefisso, numero) = dividiTelefono("+81312345678")
        assertEquals("+81312345678", componiTelefono(prefisso, numero))
    }
}
