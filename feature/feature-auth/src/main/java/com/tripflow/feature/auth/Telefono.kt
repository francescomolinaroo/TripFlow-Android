package com.tripflow.feature.auth

// Il telefono si inserisce in due parti (prefisso scelto da un elenco + numero solo cifre) e si
// salva in un unico formato internazionale, E.164: "+393331234567". Il backend accetta solo
// questo formato, quindi ogni numero nel database è scritto allo stesso modo.

internal data class Prefisso(val bandiera: String, val paese: String, val codice: String)

internal val PREFISSI = listOf(
    Prefisso("🇮🇹", "Italia", "+39"),
    Prefisso("🇸🇲", "San Marino", "+378"),
    Prefisso("🇨🇭", "Svizzera", "+41"),
    Prefisso("🇫🇷", "Francia", "+33"),
    Prefisso("🇩🇪", "Germania", "+49"),
    Prefisso("🇦🇹", "Austria", "+43"),
    Prefisso("🇪🇸", "Spagna", "+34"),
    Prefisso("🇵🇹", "Portogallo", "+351"),
    Prefisso("🇬🇧", "Regno Unito", "+44"),
    Prefisso("🇧🇪", "Belgio", "+32"),
    Prefisso("🇳🇱", "Paesi Bassi", "+31"),
    Prefisso("🇬🇷", "Grecia", "+30"),
    Prefisso("🇲🇹", "Malta", "+356"),
    Prefisso("🇷🇴", "Romania", "+40"),
    Prefisso("🇦🇱", "Albania", "+355"),
    Prefisso("🇺🇸", "Stati Uniti / Canada", "+1"),
)

internal val PREFISSO_PREDEFINITO = PREFISSI.first().codice

internal const val ERRORE_NUMERO = "Il numero deve avere tra 6 e 12 cifre"

internal fun prefissoPerCodice(codice: String): Prefisso? = PREFISSI.firstOrNull { it.codice == codice }

internal fun soloCifre(testo: String): String = testo.filter(Char::isDigit)

// Numero senza prefisso; vuoto = campo facoltativo non compilato.
internal fun isNumeroValido(numero: String): Boolean = numero.isEmpty() || numero.length in 6..12

// null se il numero è vuoto: il telefono è facoltativo, e null lo cancella dal profilo.
internal fun componiTelefono(prefisso: String, numero: String): String? =
    soloCifre(numero).takeIf { it.isNotEmpty() }?.let { prefisso + it }

// Inverso di componiTelefono, per mostrare in modifica un numero già salvato. Gestisce anche i
// numeri salvati prima del formato unico: "3331234567" (italiano senza prefisso),
// "+39 333 123 4567", "0039...". Se il prefisso non è nell'elenco si tengono le prime due cifre:
// la divisione è approssimata, ma ricomponendo si ottiene lo stesso numero.
internal fun dividiTelefono(salvato: String?): Pair<String, String> {
    val testo = salvato?.trim().orEmpty()
    val cifre = soloCifre(testo)
    if (cifre.isEmpty()) return PREFISSO_PREDEFINITO to ""

    val internazionale = when {
        testo.startsWith("+") -> "+$cifre"
        testo.startsWith("00") -> "+" + cifre.drop(2)
        cifre.length >= 11 && cifre.startsWith("39") -> "+$cifre"
        else -> return PREFISSO_PREDEFINITO to cifre
    }
    val prefisso = PREFISSI.filter { internazionale.startsWith(it.codice) }.maxByOrNull { it.codice.length }
    return if (prefisso != null) {
        prefisso.codice to internazionale.removePrefix(prefisso.codice)
    } else {
        internazionale.take(3) to internazionale.drop(3)
    }
}
