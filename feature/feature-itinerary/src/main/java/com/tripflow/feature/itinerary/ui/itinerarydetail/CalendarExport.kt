package com.tripflow.feature.itinerary.ui.itinerarydetail

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.tripflow.feature.itinerary.model.StageDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.ZoneId

// Evento di calendario ricavato da una tappa. Gli orari della tappa sono "ora locale"
// del viaggio e vengono interpretati nel fuso del telefono.
data class CalendarEvent(
    val title: String,
    val description: String,
    val location: String?,
    val beginMillis: Long,
    val endMillis: Long,
    val allDay: Boolean,
) {
    companion object {
        fun from(stage: StageDetail, itineraryTitle: String, zone: ZoneId = ZoneId.systemDefault()): CalendarEvent {
            val start = parseTime(stage.startTime)
            val description = listOfNotNull(
                stage.description?.takeIf { it.isNotBlank() },
                stage.notes?.takeIf { it.isNotBlank() },
                "Itinerario: $itineraryTitle (TripFlow)",
            ).joinToString("\n")
            val title = stage.title.ifBlank { itineraryTitle }

            if (start == null) {
                // Tappa senza orario: evento di tutta la giornata.
                val dayStart = stage.date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                return CalendarEvent(title, description, stage.location, dayStart, dayStart + DAY_MILLIS, allDay = true)
            }

            val begin = stage.date.atTime(start).atZone(zone)
            val parsedEnd = parseTime(stage.endTime)?.let { stage.date.atTime(it).atZone(zone) }
            // Fine mancante o non successiva all'inizio: durata di un'ora.
            val end = parsedEnd?.takeIf { it.isAfter(begin) } ?: begin.plusHours(1)
            return CalendarEvent(
                title = title,
                description = description,
                location = stage.location,
                beginMillis = begin.toInstant().toEpochMilli(),
                endMillis = end.toInstant().toEpochMilli(),
                allDay = false,
            )
        }

        private fun parseTime(value: String): LocalTime? =
            runCatching { LocalTime.parse(value.trim()) }.getOrNull()

        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}

object CalendarExport {

    // Apre l'app calendario con l'evento gia' compilato: nessun permesso richiesto,
    // l'utente conferma il salvataggio nell'app calendario.
    fun insertIntent(event: CalendarEvent): Intent =
        Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.beginMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.allDay)
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            event.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
        }

    sealed interface Result {
        data class Added(val count: Int) : Result
        data object NoCalendar : Result
        data class Failed(val message: String) : Result
    }

    // Inserisce piu' eventi insieme nel calendario principale del telefono.
    // Richiede READ_CALENDAR e WRITE_CALENDAR, gia' concessi dal chiamante.
    suspend fun insertAll(context: Context, events: List<CalendarEvent>): Result = withContext(Dispatchers.IO) {
        try {
            val calendarId = findWritableCalendar(context) ?: return@withContext Result.NoCalendar
            var added = 0
            events.forEach { event ->
                val values = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, event.title)
                    put(CalendarContract.Events.DESCRIPTION, event.description)
                    event.location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
                    put(CalendarContract.Events.DTSTART, event.beginMillis)
                    put(CalendarContract.Events.DTEND, event.endMillis)
                    put(CalendarContract.Events.ALL_DAY, if (event.allDay) 1 else 0)
                    put(
                        CalendarContract.Events.EVENT_TIMEZONE,
                        if (event.allDay) "UTC" else ZoneId.systemDefault().id,
                    )
                }
                if (context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values) != null) added++
            }
            Result.Added(added)
        } catch (e: SecurityException) {
            Result.Failed("Permesso al calendario non concesso")
        } catch (e: Exception) {
            Result.Failed("Impossibile aggiungere gli eventi al calendario")
        }
    }

    // Primo calendario visibile e scrivibile, preferendo quello principale dell'account.
    private fun findWritableCalendar(context: Context): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND " +
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR}"
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, projection, selection, null, null,
        )?.use { cursor ->
            var fallback: Long? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val primary = cursor.getInt(1) == 1
                if (primary) return id
                if (fallback == null) fallback = id
            }
            return fallback
        }
        return null
    }
}
