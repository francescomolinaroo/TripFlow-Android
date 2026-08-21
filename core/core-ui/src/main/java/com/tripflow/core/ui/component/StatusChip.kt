package com.tripflow.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

data class StatusStyle(
    val label: String,
    val content: Color,
    val container: Color,
)

object Status {

    fun prenotazione(stato: String?): StatusStyle = when (stato) {
        "IN_ATTESA" -> StatusStyle("IN ATTESA", TripFlowColors.Warning, TripFlowColors.WarningSoft)
        "CONFERMATA" -> StatusStyle("CONFERMATA", TripFlowColors.Success, TripFlowColors.SuccessSoft)
        "COMPLETATA" -> StatusStyle("COMPLETATA", TripFlowColors.AccentDark, TripFlowColors.AccentSoft)
        "ANNULLATA" -> StatusStyle("ANNULLATA", TripFlowColors.TextSecondary, TripFlowColors.Surface3)
        else -> StatusStyle(stato ?: "—", TripFlowColors.TextSecondary, TripFlowColors.Surface3)
    }

    fun pagamento(stato: String?): StatusStyle = when (stato) {
        "IN_ATTESA" -> StatusStyle("IN ATTESA", TripFlowColors.Warning, TripFlowColors.WarningSoft)
        "COMPLETATO" -> StatusStyle("COMPLETATO", TripFlowColors.Success, TripFlowColors.SuccessSoft)
        "FALLITO" -> StatusStyle("FALLITO", TripFlowColors.Error, TripFlowColors.ErrorSoft)
        "RIMBORSATO" -> StatusStyle("RIMBORSATO", TripFlowColors.TextSecondary, TripFlowColors.Surface3)
        else -> StatusStyle(stato ?: "—", TripFlowColors.TextSecondary, TripFlowColors.Surface3)
    }

    fun ruolo(ruolo: String?): StatusStyle = when (ruolo) {
        "ORGANIZER" -> StatusStyle("ORGANIZER", Color.White, TripFlowColors.TextPrimary)
        "TRAVELER" -> StatusStyle("VIAGGIATORE", TripFlowColors.AccentDark, TripFlowColors.AccentSoft)
        else -> StatusStyle(ruolo ?: "—", TripFlowColors.TextSecondary, TripFlowColors.Surface3)
    }
}

@Composable
fun StatusChip(
    style: StatusStyle,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(22.dp)
            .background(style.container, RoundedCornerShape(Dimens.radiusChip))
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = style.label,
            style = MaterialTheme.typography.labelSmall,
            color = style.content,
        )
    }
}

@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val container = if (highlighted) TripFlowColors.AccentSoft else TripFlowColors.Surface2
    val content = if (highlighted) TripFlowColors.AccentDark else TripFlowColors.TextBody

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(34.dp)
            .background(container, RoundedCornerShape(Dimens.radiusChip))
            .padding(horizontal = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leading?.invoke()
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = content)
        }
    }
}
