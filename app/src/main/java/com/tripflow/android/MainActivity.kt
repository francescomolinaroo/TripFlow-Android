package com.tripflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.tripflow.core.auth.AuthRepository
import com.tripflow.core.network.auth.UserResponse
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.ui.BookingDetailScreen
import com.tripflow.feature.booking.ui.BookingListScreen
import com.tripflow.feature.booking.ui.BookingScreen
import com.tripflow.feature.booking.ui.BookingUi
import com.tripflow.feature.auth.DashboardRole
import com.tripflow.feature.auth.EditProfileScreen
import com.tripflow.feature.auth.ForgotPasswordScreen
import com.tripflow.feature.auth.LoginScreen
import com.tripflow.feature.auth.RegisterScreen
import com.tripflow.feature.auth.ResetPasswordScreen
import com.tripflow.feature.auth.UserDashboardScreen
import com.tripflow.feature.auth.VerifyEmailScreen
import com.tripflow.feature.auth.dashboardRole
import com.tripflow.feature.catalog.presentation.detail.TripDetailScreen
import com.tripflow.feature.itinerary.ui.myitineraries.MyItinerariesScreen
import com.tripflow.feature.review.ui.ReviewListScreen
import com.tripflow.feature.review.ui.WriteReviewScreen
import com.tripflow.feature.catalog.presentation.discover.DiscoverScreen
import com.tripflow.feature.catalog.presentation.create.CreateTripScreen
import com.tripflow.feature.booking.ui.ReceivedBookingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                // email trasportata tra le schermate di auth (registrazione→verifica, recupero→reset)
                var authEmail by remember { mutableStateOf("") }

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
                        "booking_list" -> "discover"
                        "dashboard" -> "discover"
                        "edit_profile" -> "dashboard"
                        "review_list" -> if (reviewListTripId != null) "booking_list" else "dashboard"
                        "write_review" -> "booking_list"
                        "received_bookings" -> "discover"
                        "received_booking_detail" -> "received_bookings"
                        "create_trip" -> "dashboard"
                        "register" -> "login"
                        "forgot_password" -> "login"
                        "reset_password" -> "forgot_password"
                        "verify_email" -> "login"
                        "itinerary_list" -> "discover"
                        else -> "discover"
                    }
                }

                val tabs = tabsFor(currentUser)
                val selectedTab = tabOf(currentScreen, reviewListTripId)
                Scaffold(
                    contentWindowInsets = WindowInsets(0),
                    bottomBar = {
                        if (selectedTab != null) {
                            NavigationBar {
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = tab == selectedTab,
                                        onClick = {
                                            if (tab == MainTab.PROFILE && currentUser == null) {
                                                currentScreen = "login"
                                            } else {
                                                currentScreen = tab.route
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "checking_session" -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
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
                            "create_trip" -> CreateTripScreen(
                                onBack = { currentScreen = "dashboard" },
                                onCreated = { currentScreen = "discover" }
                            )
                            "itinerary_list" -> MyItinerariesScreen(onCreateNewClick = { /* TODO */ })
                            "login" -> LoginScreen(
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
                                onNeedsVerification = { email ->
                                    authEmail = email
                                    loginSuccessMessage = null
                                    currentScreen = "verify_email"
                                },
                                successMessage = loginSuccessMessage
                            )
                            "forgot_password" -> ForgotPasswordScreen(
                                onBackClick = { currentScreen = "login" },
                                onSuccess = { email ->
                                    authEmail = email
                                    currentScreen = "reset_password"
                                }
                            )
                            "reset_password" -> ResetPasswordScreen(
                                email = authEmail,
                                onBackClick = { currentScreen = "login" },
                                onResetComplete = {
                                    loginSuccessMessage = "Password aggiornata, ora puoi accedere"
                                    currentScreen = "login"
                                }
                            )
                            "verify_email" -> VerifyEmailScreen(
                                email = authEmail,
                                onBackClick = { currentScreen = "login" },
                                onVerified = {
                                    loginSuccessMessage = "Email verificata, ora puoi accedere"
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
                                onRegisterClick = { email ->
                                    authEmail = email
                                    loginSuccessMessage = null
                                    currentScreen = "verify_email"
                                },
                                onLoginClick = { currentScreen = "login" },
                                onBackClick = { currentScreen = "login" }
                            )
                            "dashboard" -> UserDashboardScreen(
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
                                onReceivedBookingsClick = { currentScreen = "received_bookings" },
                                onCreateTripClick = { currentScreen = "create_trip" }
                            )
                            "edit_profile" -> editingUser?.let { user ->
                                EditProfileScreen(
                                    user = user,
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
                                tripSubtitle = reviewBooking?.let { "${it.date} • ${it.location}" },
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
}

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    DISCOVER("discover", "Scopri", Icons.Filled.Explore),
    BOOKINGS("booking_list", "Prenotazioni", Icons.Filled.ConfirmationNumber),
    ITINERARIES("itinerary_list", "Itinerari", Icons.Filled.Map),
    RECEIVED("received_bookings", "Ricevute", Icons.Filled.Inbox),
    PROFILE("dashboard", "Profilo", Icons.Filled.Person)
}

fun tabsFor(user: UserResponse?): List<MainTab> {
    if (user == null) {
        return listOf(MainTab.DISCOVER, MainTab.PROFILE)
    }
    if (dashboardRole(user.role) == DashboardRole.ORGANIZER) {
        return listOf(MainTab.DISCOVER, MainTab.RECEIVED, MainTab.PROFILE)
    }
    return listOf(MainTab.DISCOVER, MainTab.BOOKINGS, MainTab.ITINERARIES, MainTab.PROFILE)
}

fun tabOf(screen: String, reviewListTripId: String?): MainTab? {
    return when (screen) {
        "discover", "trip_detail" -> MainTab.DISCOVER
        "booking_list", "booking_detail" -> MainTab.BOOKINGS
        "itinerary_list" -> MainTab.ITINERARIES
        "received_bookings", "received_booking_detail" -> MainTab.RECEIVED
        "dashboard" -> MainTab.PROFILE
        "review_list" -> if (reviewListTripId != null) MainTab.BOOKINGS else MainTab.PROFILE
        else -> null
    }
}
