package com.tripflow.core.ui.format

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {

    private val moneyFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.ITALY))

    private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)
    private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)
    private val dayOnly = DateTimeFormatter.ofPattern("d", Locale.ITALIAN)
    private val timeOnly = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN)
    private val dayMonthTime = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ITALIAN)

    fun money(value: BigDecimal?): String =
        if (value == null) "—" else synchronized(moneyFormat) { "€ ${moneyFormat.format(value)}" }

    fun date(value: LocalDate?): String = value?.format(dayMonthYear) ?: "—"

    fun dateRange(start: LocalDate?, end: LocalDate?): String = when {
        start == null && end == null -> "—"
        start == null -> "fino al ${date(end)}"
        end == null -> "dal ${date(start)}"
        start.month == end.month && start.year == end.year ->
            "${start.format(dayOnly)} – ${end.format(dayMonthYear)}"
        start.year == end.year ->
            "${start.format(dayMonth)} – ${end.format(dayMonthYear)}"
        else -> "${date(start)} – ${date(end)}"
    }

    fun dateTime(value: LocalDateTime?): String = value?.format(dayMonthTime) ?: "—"

    fun timeRange(start: LocalDateTime?, end: LocalDateTime?): String = when {
        start == null -> "—"
        end == null -> start.format(timeOnly)
        else -> "${start.format(timeOnly)} – ${end.format(timeOnly)}"
    }

    fun duration(minutes: Int?): String = when {
        minutes == null || minutes <= 0 -> "—"
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }

    fun partecipanti(n: Int): String = if (n == 1) "1 partecipante" else "$n partecipanti"

    fun postiLiberi(availableSpots: Int?, bookedSpots: Int?): Int =
        ((availableSpots ?: 0) - (bookedSpots ?: 0)).coerceAtLeast(0)
}
