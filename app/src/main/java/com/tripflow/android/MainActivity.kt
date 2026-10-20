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
import com.tripflow.feature.booking.ui.BookingDetailScreen
import com.tripflow.feature.booking.ui.BookingListScreen
import com.tripflow.feature.booking.ui.BookingScreen
import com.tripflow.feature.booking.ui.BookingUi
import com.tripflow.feature.auth.EditProfileScreen
import com.tripflow.feature.auth.ForgotPasswordScreen
import com.tripflow.feature.auth.LoginScreen
import com.tripflow.feature.auth.RegisterScreen
import com.tripflow.feature.auth.UserDashboardScreen
import com.tripflow.feature.catalog.presentation.detail.TripDetailScreen
import com.tripflow.feature.itinerary.ui.myitineraries.MyItinerariesScreen
import com.tripflow.feature.review.ui.ReviewListScreen
import com.tripflow.feature.review.ui.WriteReviewScreen
import com.tripflow.feature.catalog.presentation.discover.DiscoverScreen
import com.tripflow.feature.booking.ui.ReceivedBookingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authRepository = AuthRepository(this)
        setContent {
            TripFlowTheme {
                var currentScreen by remember { mutableStateOf("checking_session") }
                var currentUser by remember { mutableStateOf<UserResponse?>(null) }
                var editingUser by remember { mutableStateOf<UserResponse?>(null) }
                var selectedTripId by remember { mutableStateOf<String?>(null) }
                var selectedBookingId by remember { mutableStateOf<String?>(null) }
                var reviewBooking by remember { mutableStateOf<BookingUi?>(null) }
                var reviewListTripId by remember { mutableStateOf<String?>(null) }
                var loginSuccessMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val sessionResult = authRepository.checkSession()
                    if (sessionResult.isSuccess) {
                        currentUser = sessionResult.getOrNull()
                        currentScreen = "discover"
                    } else {
                        currentScreen = "login"
                    }
                }

                BackHandler(enabled = currentScreen !in setOf("discover", "login", "checking_session")) {
                    currentScreen = when (currentScreen) {
                        "booking_form" -> "trip_detail"
                        "trip_detail" -> "discover"
                        "booking_detail" -> "booking_list"
                        "booking_list" -> "dashboard"
                        "dashboard" -> "discover"
                        "edit_profile" -> "dashboard"
                        "review_list" -> if (reviewListTripId != null) "booking_list" else "dashboard"
                        "write_review" -> "booking_list"
                        "received_bookings" -> "menu"
                        "received_booking_detail" -> "received_bookings"
                        "register" -> "login"
                        "forgot_password" -> "login"
                        "itinerary_list" -> "dashboard"
                        else -> "discover"
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (currentScreen) {
                        "checking_session" -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        "menu" -> MenuScreen(onNavigate = { currentScreen = it })
                        "booking_form" -> BookingScreen(
                            viaggioId = selectedTripId,
                            onBack = { currentScreen = "trip_detail" },
                            onPaymentClick = { bookingId ->
                                selectedBookingId = bookingId
                                currentScreen = "booking_detail"
                            }
                        )
                        "booking_list" -> BookingListScreen(
                            onBookingClick = { bookingId ->
                                selectedBookingId = bookingId
                                currentScreen = "booking_detail"
                            },
                            onWriteReviewClick = { booking ->
                                reviewBooking = booking
                                currentScreen = "write_review"
                            },
                            onPayClick = { bookingId ->
                                selectedBookingId = bookingId
                                currentScreen = "booking_detail"
                            }
                        )
                        "booking_detail" -> BookingDetailScreen(
                            prenotazioneId = selectedBookingId,
                            onBack = { currentScreen = "booking_list" }
                        )
                        "received_bookings" -> ReceivedBookingsScreen(
                            onBack = { currentScreen = "menu" },
                            onBookingClick = { bookingId ->
                                selectedBookingId = bookingId
                                currentScreen = "received_booking_detail"
                            }
                        )
                        "received_booking_detail" -> BookingDetailScreen(
                            prenotazioneId = selectedBookingId,
                            vistaOrganizzatore = true,
                            onBack = { currentScreen = "received_bookings" }
                        )
                        "itinerary_list" -> MyItinerariesScreen(onCreateNewClick = { /* TODO */ })
                        "login" -> LoginScreen(
                            authRepository = authRepository,
                            onLoginClick = { user ->
                                currentUser = user
                                loginSuccessMessage = null
                                currentScreen = "discover"
                            },
                            onRegisterClick = {
                                loginSuccessMessage = null
                                currentScreen = "register"
                            },
                            onForgotPasswordClick = {
                                loginSuccessMessage = null
                                currentScreen = "forgot_password"
                            },
                            onContinueWithoutLogin = {
                                currentUser = null
                                loginSuccessMessage = null
                                currentScreen = "discover"
                            },
                            successMessage = loginSuccessMessage
                        )
                        "forgot_password" -> ForgotPasswordScreen(
                            authRepository = authRepository,
                            onBackClick = { currentScreen = "login" },
                            onSuccess = {
                                loginSuccessMessage = "Se l'indirizzo email esiste, riceverai una mail con le istruzioni"
                                currentScreen = "login"
                            }
                        )
                        "discover" -> DiscoverScreen(
                            onTripClick = { tripId ->
                                selectedTripId = tripId
                                currentScreen = "trip_detail"
                            },
                            onProfileClick = {
                                currentScreen = if (currentUser != null) "dashboard" else "login"
                            }
                        )
                        "trip_detail" -> {
                            selectedTripId?.let { id ->
                                TripDetailScreen(
                                    tripId = id,
                                    onBackClick = { currentScreen = "discover" },
                                    onBookClick = { tripIdToBook ->
                                        selectedTripId = tripIdToBook
                                        currentScreen = "booking_form"
                                    }
                                )
                            } ?: run {
                                currentScreen = "discover"
                            }
                        }
                        "register" -> RegisterScreen(
                            authRepository = authRepository,
                            onRegisterClick = {
                                loginSuccessMessage = "Registrazione completata, ora puoi accedere"
                                currentScreen = "login"
                            },
                            onLoginClick = { currentScreen = "login" },
                            onBackClick = { currentScreen = "login" }
                        )
                        "dashboard" -> UserDashboardScreen(
                            authRepository = authRepository,
                            onBackClick = { currentScreen = "discover" },
                            onLogoutClick = {
                                currentUser = null
                                currentScreen = "login"
                            },
                            onSessionExpired = {
                                currentUser = null
                                currentScreen = "login"
                            },
                            onEditProfileClick = { user ->
                                editingUser = user
                                currentScreen = "edit_profile"
                            },
                            onBookingsClick = { currentScreen = "booking_list" },
                            onReviewsClick = {
                                reviewListTripId = null
                                currentScreen = "review_list"
                            },
                            onMyTripsClick = { currentScreen = "itinerary_list" },
                            onReceivedBookingsClick = { currentScreen = "received_bookings" }
                        )
                        "edit_profile" -> editingUser?.let { user ->
                            EditProfileScreen(
                                user = user,
                                authRepository = authRepository,
                                onBackClick = { currentScreen = "dashboard" },
                                onSaved = { updatedUser ->
                                    currentUser = updatedUser
                                    editingUser = updatedUser
                                    currentScreen = "dashboard"
                                },
                                onAccountDeleted = {
                                    currentUser = null
                                    editingUser = null
                                    currentScreen = "login"
                                }
                            )
                        }
                        "review_list" -> ReviewListScreen(
                            oggettoId = reviewListTripId,
                            onBackClick = {
                                currentScreen = if (reviewListTripId != null) "booking_list" else "dashboard"
                            }
                        )
                        "write_review" -> WriteReviewScreen(
                            prenotazioneId = reviewBooking?.id,
                            viaggioId = reviewBooking?.tripId,
                            tripTitle = reviewBooking?.title ?: "Viaggio",
                            tripSubtitle = reviewBooking?.let { "${it.date} \u2022 ${it.location}" },
                            onBack = { currentScreen = "booking_list" },
                            onPublish = {
                                reviewListTripId = reviewBooking?.tripId
                                currentScreen = "review_list"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuScreen(onNavigate: (String) -> Unit) {
    Scaffold { padding ->
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
            Button(onClick = { onNavigate("received_bookings") }, modifier = Modifier.fillMaxWidth()) {
                Text("Prenotazioni ricevute")
            }
            Button(onClick = { onNavigate("login") }, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
            Button(onClick = { onNavigate("forgot_password") }, modifier = Modifier.fillMaxWidth()) {
                Text("Password dimenticata")
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
            Button(onClick = { onNavigate("discover") }, modifier = Modifier.fillMaxWidth()) {
                Text("Scopri (catalogo viaggi)")
            }
        }
    }
}
