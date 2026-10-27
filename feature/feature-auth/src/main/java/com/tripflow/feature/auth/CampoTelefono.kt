package com.tripflow.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tripflow.core.ui.component.TripFlowTextField
import com.tripflow.core.ui.theme.TripFlowColors

// Campo telefono con il prefisso internazionale scelto da un menu dentro il campo stesso:
// il prefisso non si scrive a mano, il numero accetta solo cifre.
@Composable
internal fun CampoTelefono(
    prefisso: String,
    numero: String,
    onPrefissoChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    error: String?,
) {
    var menuAperto by remember { mutableStateOf(false) }
    val corrente = prefissoPerCodice(prefisso)

    TripFlowTextField(
        value = numero,
        onValueChange = onNumeroChange,
        label = "Telefono",
        placeholder = "333 123 4567",
        error = error,
        isValid = numero.isNotEmpty() && error == null,
        keyboardType = KeyboardType.Number,
        leading = {
            Box {
                TextButton(
                    onClick = { menuAperto = true },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = corrente?.let { "${it.bandiera} ${it.codice}" } ?: prefisso,
                        color = TripFlowColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Scegli il prefisso", tint = TripFlowColors.TextSecondary)
                }
                DropdownMenu(expanded = menuAperto, onDismissRequest = { menuAperto = false }) {
                    PREFISSI.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.bandiera}  ${p.paese}  ${p.codice}") },
                            onClick = {
                                onPrefissoChange(p.codice)
                                menuAperto = false
                            }
                        )
                    }
                }
            }
        }
    )
}
