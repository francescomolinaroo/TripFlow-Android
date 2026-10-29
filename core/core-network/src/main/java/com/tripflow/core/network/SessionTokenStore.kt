package com.tripflow.core.network

// Accesso ai token di sessione che serve all'Authenticator per rinnovarli.
// Lo implementa chi i token li tiene salvati davvero (TokenStorage): in questo modo
// core-network puo' fare il refresh senza dipendere da core-auth, che invece dipende da qui.
interface SessionTokenStore {
    fun accessToken(): String?
    fun refreshToken(): String?
    fun save(accessToken: String, refreshToken: String?)
    fun clear()
}
