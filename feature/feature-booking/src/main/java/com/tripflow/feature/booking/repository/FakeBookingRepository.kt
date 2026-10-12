package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.feature.booking.model.MetodoPagamento
import com.tripflow.feature.booking.model.PagamentoIntentResponse
import com.tripflow.feature.booking.model.PagamentoResponse
import com.tripflow.feature.booking.model.PrenotazioneAttivitaResponse
import com.tripflow.feature.booking.model.PrenotazioneRequest
import com.tripflow.feature.booking.model.PrenotazioneResponse
import com.tripflow.feature.booking.model.StatoPagamento
import com.tripflow.feature.booking.model.StatoPrenotazione
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class FakeBookingRepository(
    private val erroreForzato: UiState.Error? = null,
    private val latenzaMs: Long = 600,
    private val catalogo: CatalogoRepository? = null,
    private val ritardoWebhookMs: Long? = 4000
) : BookingRepository {

    private val webhookInArrivo = mutableMapOf<String, Long>()

    private data class ViaggioFake(
        val titolo: String,
        val destinazione: String,
        val dataInizio: LocalDate,
        val dataFine: LocalDate,
        val prezzo: BigDecimal,
        val postiDisponibili: Int
    )

    private data class AttivitaFake(
        val viaggioId: String,
        val nome: String,
        val prezzo: BigDecimal,
        val durataMinuti: Int
    )

    private val viaggi = mutableMapOf(
        VIAGGIO_AMALFI to ViaggioFake(
            "Costa Amalfitana", "Amalfi",
            LocalDate.of(2026, 10, 14), LocalDate.of(2026, 10, 18), BigDecimal("480"), 12
        ),
        VIAGGIO_FUNES to ViaggioFake(
            "Trekking in Val di Funes", "Bolzano",
            LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 27), BigDecimal("620"), 10
        ),
        VIAGGIO_EOLIE to ViaggioFake(
            "Isole Eolie in barca", "Lipari",
            LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 7), BigDecimal("445"), 8
        )
    )

    private val attivita = mutableMapOf(
        ATTIVITA_BARCA_CAPRI to AttivitaFake(VIAGGIO_AMALFI, "Tour in barca a Capri", BigDecimal("45"), 240),
        ATTIVITA_LIMONCELLO to AttivitaFake(VIAGGIO_AMALFI, "Degustazione di limoncello", BigDecimal("30"), 90),
        ATTIVITA_SENTIERO_DEI to AttivitaFake(VIAGGIO_AMALFI, "Sentiero degli Dei", BigDecimal("25"), 300)
    )

    private val prenotazioni = mutableListOf(
        costruisci(
            viaggioId = VIAGGIO_AMALFI,
            partecipanti = 2,
            attivitaIds = listOf(ATTIVITA_BARCA_CAPRI),
            scadenza = LocalDateTime.now().plusMinutes(8)
        ),
        costruisci(
            viaggioId = VIAGGIO_FUNES,
            partecipanti = 1,
            stato = StatoPrenotazione.CONFERMATA,
            pagamentoCompletato = true
        ),
        costruisci(
            viaggioId = VIAGGIO_EOLIE,
            partecipanti = 2,
            stato = StatoPrenotazione.COMPLETATA,
            note = "Cabina doppia se possibile",
            pagamentoCompletato = true
        )
    )

    override suspend fun creaPrenotazione(request: PrenotazioneRequest): UiState<PrenotazioneResponse> = simula {
        val viaggio = viaggi[request.viaggioId] ?: when (val result = catalogo?.trovaViaggio(request.viaggioId)) {
            is UiState.Success -> registra(result.data)
                ?: return@simula errore("Viaggio ${request.viaggioId} con date non valide nel catalog")
            is UiState.Error -> return@simula result
            else -> return@simula errore("Viaggio non trovato nel catalog: ${request.viaggioId}")
        }
        if (request.numeroPartecipanti !in 1..50) {
            return@simula errore("numeroPartecipanti : Specificare tra 1 e 50 partecipanti")
        }
        if (!viaggio.dataInizio.isAfter(LocalDate.now())) {
            return@simula errore(
                "Impossibile prenotare: il viaggio ${request.viaggioId} è iniziato il ${viaggio.dataInizio}"
            )
        }
        val occupati = prenotazioni
            .filter { it.viaggioId == request.viaggioId && it.stato in STATI_ATTIVI }
            .sumOf { it.numeroPartecipanti }
        val disponibili = viaggio.postiDisponibili - occupati
        if (request.numeroPartecipanti > disponibili) {
            return@simula errore(
                "Posti insufficienti per il viaggio ${request.viaggioId}: " +
                    "disponibili $disponibili, richiesti ${request.numeroPartecipanti}"
            )
        }
        val estranea = request.attivitaIds.firstOrNull { attivita[it]?.viaggioId != request.viaggioId }
        if (estranea != null) {
            return@simula errore("Attività $estranea non appartiene al viaggio ${request.viaggioId}")
        }
        val nuova = costruisci(
            viaggioId = request.viaggioId,
            partecipanti = request.numeroPartecipanti,
            attivitaIds = request.attivitaIds.distinct(),
            note = request.note,
            scadenza = LocalDateTime.now().plus(DURATA_BLOCCO)
        ).aggiornata()
        prenotazioni.add(0, nuova)
        UiState.Success(nuova)
    }

    override suspend fun trovaPrenotazione(id: String): UiState<PrenotazioneResponse> = simula {
        val p = trova(id) ?: return@simula nonTrovata(id)
        UiState.Success(p)
    }

    override suspend fun miePrenotazioni(): UiState<List<PrenotazioneResponse>> = simula {
        UiState.Success(prenotazioni.toList())
    }

    override suspend fun miePrenotazioniAttive(): UiState<List<PrenotazioneResponse>> = simula {
        UiState.Success(prenotazioni.filter { it.stato in STATI_ATTIVI })
    }

    override suspend fun annullaPrenotazione(id: String): UiState<PrenotazioneResponse> = simula {
        val p = trova(id) ?: return@simula nonTrovata(id)
        if (p.stato !in STATI_ATTIVI) {
            return@simula errore("Impossibile annullare: prenotazione in stato ${p.stato}")
        }
        webhookInArrivo.remove(id)
        val pagamento = p.infoPagamento?.let {
            if (p.stato == StatoPrenotazione.CONFERMATA && it.stato == StatoPagamento.COMPLETATO) {
                it.copy(stato = StatoPagamento.RIMBORSATO)
            } else {
                it
            }
        }
        salva(p.copy(stato = StatoPrenotazione.ANNULLATA, secondiAllaScadenza = null, infoPagamento = pagamento))
    }

    override suspend fun aggiungiAttivita(prenotazioneId: String, attivitaId: String): UiState<PrenotazioneResponse> = simula {
        val p = trova(prenotazioneId) ?: return@simula nonTrovata(prenotazioneId)
        if (p.stato != StatoPrenotazione.IN_ATTESA) {
            return@simula errore("Impossibile aggiungere attività: prenotazione in stato ${p.stato}")
        }
        if (p.attivitaSelezionate.orEmpty().any { it.attivitaId == attivitaId }) {
            return@simula errore("Attività $attivitaId già presente nella prenotazione")
        }
        val att = attivita[attivitaId] ?: return@simula errore("Attività non trovata nel catalog: $attivitaId")
        if (att.viaggioId != p.viaggioId) {
            return@simula errore("Attività $attivitaId non appartiene al viaggio ${p.viaggioId}")
        }
        salva(
            p.copy(attivitaSelezionate = p.attivitaSelezionate.orEmpty() + snapshot(attivitaId, att))
                .conPrezzoRicalcolato()
        )
    }

    override suspend fun rimuoviAttivita(prenotazioneId: String, attivitaId: String): UiState<PrenotazioneResponse> = simula {
        val p = trova(prenotazioneId) ?: return@simula nonTrovata(prenotazioneId)
        if (p.stato != StatoPrenotazione.IN_ATTESA) {
            return@simula errore("Impossibile rimuovere attività: prenotazione in stato ${p.stato}")
        }
        if (p.attivitaSelezionate.orEmpty().none { it.attivitaId == attivitaId }) {
            return@simula errore("Attività $attivitaId non presente nella prenotazione")
        }
        salva(
            p.copy(attivitaSelezionate = p.attivitaSelezionate.orEmpty().filter { it.attivitaId != attivitaId })
                .conPrezzoRicalcolato()
        )
    }

    override suspend fun avviaPagamento(prenotazioneId: String): UiState<PagamentoIntentResponse> = simula {
        val p = trova(prenotazioneId) ?: return@simula nonTrovata(prenotazioneId)
        if (p.stato != StatoPrenotazione.IN_ATTESA) {
            return@simula errore("Impossibile avviare pagamento: prenotazione in stato ${p.stato}")
        }
        if (p.infoPagamento != null && p.infoPagamento.stato != StatoPagamento.FALLITO) {
            return@simula errore("Esiste già un pagamento per la prenotazione $prenotazioneId")
        }
        val pagamento = PagamentoResponse(
            id = UUID.randomUUID().toString(),
            importo = p.prezzoTotale,
            metodo = null,
            stato = StatoPagamento.IN_ATTESA,
            ultimeQuattroCifre = null,
            brandCarta = null,
            dataPagamento = null,
            createdAt = LocalDateTime.now().toString(),
            stripePaymentIntentId = "pi_fake_${UUID.randomUUID().toString().take(8)}"
        )
        salva(p.copy(infoPagamento = pagamento))
        ritardoWebhookMs?.let { webhookInArrivo[prenotazioneId] = System.currentTimeMillis() + it }
        UiState.Success(
            PagamentoIntentResponse(
                pagamentoId = pagamento.id,
                clientSecret = "${pagamento.stripePaymentIntentId}_secret_fake",
                importo = pagamento.importo
            )
        )
    }

    override suspend fun trovaPagamento(prenotazioneId: String): UiState<PagamentoResponse> = simula {
        val p = trova(prenotazioneId) ?: return@simula nonTrovata(prenotazioneId)
        val pagamento = p.infoPagamento
            ?: return@simula UiState.Error("Nessun pagamento trovato per la prenotazione $prenotazioneId", retryable = false)
        UiState.Success(pagamento)
    }

    fun simulaWebhookPagamento(prenotazioneId: String, successo: Boolean = true) {
        val p = trova(prenotazioneId) ?: return
        val pagamento = p.infoPagamento ?: return
        if (successo) {
            salva(
                p.copy(
                    stato = StatoPrenotazione.CONFERMATA,
                    secondiAllaScadenza = null,
                    infoPagamento = pagamento.completato()
                )
            )
        } else {
            salva(p.copy(infoPagamento = pagamento.copy(stato = StatoPagamento.FALLITO)))
        }
    }

    private suspend fun <T> simula(block: suspend () -> UiState<T>): UiState<T> {
        delay(latenzaMs)
        erroreForzato?.let { return it }
        val adesso = System.currentTimeMillis()
        webhookInArrivo.filterValues { it <= adesso }.keys.forEach { id ->
            webhookInArrivo.remove(id)
            simulaWebhookPagamento(id)
        }
        prenotazioni.replaceAll { it.aggiornata() }
        return block()
    }

    private fun registra(trip: TripResponseDTO): ViaggioFake? {
        val inizio = runCatching { LocalDate.parse(trip.startDate) }.getOrNull() ?: return null
        val fine = runCatching { LocalDate.parse(trip.endDate) }.getOrNull() ?: return null
        trip.activities.orEmpty().forEach {
            attivita[it.id] = AttivitaFake(trip.id, it.name, BigDecimal.valueOf(it.price), it.duration)
        }
        return ViaggioFake(
            titolo = trip.name,
            destinazione = trip.destination,
            dataInizio = inizio,
            dataFine = fine,
            prezzo = BigDecimal.valueOf(trip.price),
            postiDisponibili = trip.availableSpots
        ).also { viaggi[trip.id] = it }
    }

    private fun trova(id: String): PrenotazioneResponse? = prenotazioni.find { it.id == id }

    private fun salva(prenotazione: PrenotazioneResponse): UiState<PrenotazioneResponse> {
        val index = prenotazioni.indexOfFirst { it.id == prenotazione.id }
        if (index >= 0) prenotazioni[index] = prenotazione
        return UiState.Success(prenotazione)
    }

    private fun nonTrovata(id: String) =
        UiState.Error("Prenotazione non trovata con id: $id", retryable = false)

    private fun errore(message: String) = UiState.Error(message, retryable = false)

    private fun costruisci(
        viaggioId: String,
        partecipanti: Int,
        attivitaIds: List<String> = emptyList(),
        stato: StatoPrenotazione = StatoPrenotazione.IN_ATTESA,
        note: String? = null,
        scadenza: LocalDateTime? = null,
        pagamentoCompletato: Boolean = false
    ): PrenotazioneResponse {
        val viaggio = viaggi.getValue(viaggioId)
        val prenotazione = PrenotazioneResponse(
            id = UUID.randomUUID().toString(),
            viaggioId = viaggioId,
            titoloViaggio = viaggio.titolo,
            destinazione = viaggio.destinazione,
            dataInizio = viaggio.dataInizio.toString(),
            dataFine = viaggio.dataFine.toString(),
            prezzoUnitarioAlMomentoDelBooking = viaggio.prezzo,
            numeroPartecipanti = partecipanti,
            prezzoTotale = BigDecimal.ZERO,
            stato = stato,
            scadenzaIl = scadenza?.toString(),
            secondiAllaScadenza = null,
            note = note,
            attivitaSelezionate = attivitaIds.map { snapshot(it, attivita.getValue(it)) },
            infoPagamento = null
        ).conPrezzoRicalcolato()

        if (!pagamentoCompletato) return prenotazione
        return prenotazione.copy(
            infoPagamento = PagamentoResponse(
                id = UUID.randomUUID().toString(),
                importo = prenotazione.prezzoTotale,
                metodo = null,
                stato = StatoPagamento.IN_ATTESA,
                ultimeQuattroCifre = null,
                brandCarta = null,
                dataPagamento = null,
                createdAt = LocalDateTime.now().minusDays(30).toString(),
                stripePaymentIntentId = "pi_fake_${UUID.randomUUID().toString().take(8)}"
            ).completato()
        )
    }

    private fun snapshot(attivitaId: String, att: AttivitaFake) = PrenotazioneAttivitaResponse(
        id = UUID.randomUUID().toString(),
        attivitaId = attivitaId,
        nome = att.nome,
        prezzo = att.prezzo,
        durataMinuti = att.durataMinuti,
        aggiuntoIl = LocalDateTime.now().toString()
    )

    private fun PrenotazioneResponse.conPrezzoRicalcolato(): PrenotazioneResponse {
        val unitario = (prezzoUnitarioAlMomentoDelBooking ?: BigDecimal.ZERO) +
            attivitaSelezionate.orEmpty().sumOf { it.prezzo }
        return copy(prezzoTotale = unitario * BigDecimal(numeroPartecipanti))
    }

    private fun PrenotazioneResponse.aggiornata(): PrenotazioneResponse {
        if (stato != StatoPrenotazione.IN_ATTESA || scadenzaIl == null) {
            return copy(secondiAllaScadenza = null)
        }
        val secondi = Duration.between(LocalDateTime.now(), LocalDateTime.parse(scadenzaIl)).seconds
        return if (secondi > 0) {
            copy(secondiAllaScadenza = secondi)
        } else {
            copy(stato = StatoPrenotazione.SCADUTA, secondiAllaScadenza = null)
        }
    }

    private fun PagamentoResponse.completato() = copy(
        metodo = MetodoPagamento.CARTA_CREDITO,
        stato = StatoPagamento.COMPLETATO,
        ultimeQuattroCifre = "4242",
        brandCarta = "visa",
        dataPagamento = LocalDateTime.now().toString()
    )

    companion object {
        const val VIAGGIO_AMALFI = "a1000000-0000-0000-0000-000000000001"
        const val VIAGGIO_FUNES = "a1000000-0000-0000-0000-000000000002"
        const val VIAGGIO_EOLIE = "a1000000-0000-0000-0000-000000000003"

        const val ATTIVITA_BARCA_CAPRI = "b2000000-0000-0000-0000-000000000001"
        const val ATTIVITA_LIMONCELLO = "b2000000-0000-0000-0000-000000000002"
        const val ATTIVITA_SENTIERO_DEI = "b2000000-0000-0000-0000-000000000003"
        private val DURATA_BLOCCO: Duration = Duration.ofMinutes(10)
        private val STATI_ATTIVI = setOf(StatoPrenotazione.IN_ATTESA, StatoPrenotazione.CONFERMATA)
    }
}
