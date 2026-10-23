package com.tripflow.core.auth

// Segnala che il login è stato rifiutato dal backend (403) perché l'email non è ancora
// stata verificata. Permette alla UI di distinguere questo caso dalle credenziali errate.
class EmailNotVerifiedException(val email: String) : Exception("Email non verificata")
