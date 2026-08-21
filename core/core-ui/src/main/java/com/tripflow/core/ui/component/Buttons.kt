package com.tripflow.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.radiusButton),
        contentPadding = PaddingValues(horizontal = Dimens.gapL),
        colors = ButtonDefaults.buttonColors(
            containerColor = TripFlowColors.Accent,
            contentColor = Color.White,
            disabledContainerColor = TripFlowColors.Surface3,
            disabledContentColor = TripFlowColors.TextDisabled,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun NeutralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.radiusButton),
        colors = ButtonDefaults.buttonColors(
            containerColor = TripFlowColors.TextPrimary,
            contentColor = Color.White,
            disabledContainerColor = TripFlowColors.Surface3,
            disabledContentColor = TripFlowColors.TextDisabled,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.radiusButton),
        border = BorderStroke(1.dp, TripFlowColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TripFlowColors.TextPrimary,
            disabledContentColor = TripFlowColors.TextDisabled,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.radiusButton),
        border = BorderStroke(1.5.dp, TripFlowColors.ErrorBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TripFlowColors.Error,
            disabledContentColor = TripFlowColors.TextDisabled,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
