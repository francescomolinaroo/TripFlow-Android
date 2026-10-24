package com.tripflow.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

// Cambia a ogni login/logout: usata come key dei ViewModel con dati dell'utente,
// così non riusano lo stato dell'account precedente.
val LocalSessionKey = staticCompositionLocalOf { "" }
