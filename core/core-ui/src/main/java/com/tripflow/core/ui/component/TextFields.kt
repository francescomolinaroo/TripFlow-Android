package com.tripflow.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun TripFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val borderColor = when {
        error != null -> TripFlowColors.FieldError
        value.isNotEmpty() -> TripFlowColors.Accent
        else -> TripFlowColors.Border
    }
    val borderWidth = if (error != null || value.isNotEmpty()) 1.5.dp else 1.dp
    val shape = RoundedCornerShape(Dimens.radiusField)

    Column(modifier = modifier.fillMaxWidth()) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextBody,
            modifier = Modifier.padding(bottom = Dimens.gapS),
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TripFlowColors.TextPrimary),
            cursorBrush = SolidColor(TripFlowColors.Accent),
            visualTransformation =
                if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            ),
            modifier = Modifier.fillMaxWidth().height(Dimens.fieldHeight),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TripFlowColors.Background, shape)
                        .border(borderWidth, borderColor, shape)
                        .padding(horizontal = 14.dp),
                ) {
                    leading?.invoke()
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TripFlowColors.TextSecondary,
                            )
                        }
                        innerTextField()
                    }
                    trailing?.invoke()
                }
            },
        )

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.FieldError,
                modifier = Modifier.padding(top = Dimens.gapS),
            )
        }
    }
}

@Composable
fun TripFlowTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxChars: Int,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    minHeight: Dp = 96.dp,
) {
    val shape = RoundedCornerShape(Dimens.radiusField)
    val borderColor = when {
        error != null -> TripFlowColors.FieldError
        value.isNotEmpty() -> TripFlowColors.Accent
        else -> TripFlowColors.Border
    }

    Column(modifier = modifier.fillMaxWidth()) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TripFlowColors.TextBody,
            modifier = Modifier.padding(bottom = Dimens.gapS),
        )

        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxChars) onValueChange(it) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TripFlowColors.TextBody),
            cursorBrush = SolidColor(TripFlowColors.Accent),
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TripFlowColors.Background, shape)
                        .border(if (error != null || value.isNotEmpty()) 1.5.dp else 1.dp, borderColor, shape)
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TripFlowColors.TextSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = TripFlowColors.FieldError,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            Text(
                text = "${value.length} / $maxChars",
                style = MaterialTheme.typography.bodySmall,
                color = TripFlowColors.TextDisabled,
                textAlign = TextAlign.End,
            )
        }
    }
}
