package com.tripflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.ui.BookingListScreen
import com.tripflow.feature.booking.ui.BookingScreen
import com.tripflow.feature.auth.EditProfileScreen
import com.tripflow.feature.auth.LoginScreen
import com.tripflow.feature.auth.RegisterScreen
import com.tripflow.feature.auth.UserDashboardScreen
import com.tripflow.feature.itinerary.ui.myitineraries.MyItinerariesScreen
import com.tripflow.feature.review.ReviewListScreen
import com.tripflow.feature.review.WriteReviewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authRepository = AuthRepository(this)
        setContent {
            TripFlowTheme {
                var currentScreen by remember { mutableStateOf("checking_session") }
                var editingUser by remember { mutableStateOf<UserResponse?>(null) }

                LaunchedEffect(Unit) {
                    currentScreen = if (authRepository.checkSession().isSuccess) {
                        "dashboard"
                    } else {
                        "login"
                    }
                }

                BackHandler(enabled = currentScreen != "menu") {
                    currentScreen = when (currentScreen) {
                        "booking_form", "booking_list", "login", "register" -> "menu"
                        "dashboard" -> "menu"
                        "edit_profile", "review_list" -> "dashboard"
                        else -> "menu"
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (currentScreen) {
                        "checking_session" -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        "menu" -> MenuScreen(onNavigate = { currentScreen = it })
                        "booking_form" -> BookingScreen(onBack = { currentScreen = "menu" })
                        "booking_list" -> BookingListScreen(
                            onBookingClick = { currentScreen = "menu" },
                            onWriteReviewClick = { currentScreen = "write_review" }
                        )
                        "itinerary_list" -> MyItinerariesScreen(onCreateNewClick = { /* TODO */ })
                        "login" -> LoginScreen(
                            authRepository = authRepository,
                            onLoginClick = { currentScreen = "dashboard" },
                            onRegisterClick = { currentScreen = "register" },
                            onContinueWithoutLogin = { currentScreen = "menu" }
                        )
                        "register" -> RegisterScreen(
                            onRegisterClick = { currentScreen = "login" },
                            onLoginClick = { currentScreen = "login" },
                            onBackClick = { currentScreen = "login" }
                        )
                        "dashboard" -> UserDashboardScreen(
                            authRepository = authRepository,
                            onLogoutClick = { currentScreen = "login" },
                            onSessionExpired = { currentScreen = "login" },
                            onEditProfileClick = { user ->
                                editingUser = user
                                currentScreen = "edit_profile"
                            },
                            onBookingsClick = { currentScreen = "booking_list" },
                            onReviewsClick = { currentScreen = "review_list" }
                        )
                        "edit_profile" -> editingUser?.let { user ->
                            EditProfileScreen(
                                user = user,
                                authRepository = authRepository,
                                onBackClick = { currentScreen = "dashboard" },
                                onSaved = { currentScreen = "dashboard" }
                            )
                        }
                        "review_list" -> ReviewListScreen(
                            onBackClick = { currentScreen = "dashboard" }
                        )
                        "write_review" -> WriteReviewScreen(onBack = { currentScreen = "menu" })
                    }
                }
            }
        }
    }
}

@Composable
fun MenuScreen(onNavigate: (String) -> Unit) { //menu temporaneo per provare le mie schermate. Aggiungete le vostre finchè non creiamo il menu
    Scaffold { padding -> // di navigazione definitivo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TripFlow Demo", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onNavigate("booking_form") }, modifier = Modifier.fillMaxWidth()) {
                Text("Booking Form")
            }
            Button(onClick = { onNavigate("booking_list") }, modifier = Modifier.fillMaxWidth()) {
                Text("My Bookings")
            }
            Button(onClick = { onNavigate("login") }, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
            Button(onClick = { onNavigate("register") }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrati")
            }
            Button(onClick = { onNavigate("review_list") }, modifier = Modifier.fillMaxWidth()) {
                Text("Review")
            }
            Button(onClick = { onNavigate("write_review") }, modifier = Modifier.fillMaxWidth()) {
                Text("Write Review")
            }
            Button(onClick = { onNavigate("itinerary_list") }, modifier = Modifier.fillMaxWidth()) {
                Text("I miei Itinerari")
            }
        }
    }
}


