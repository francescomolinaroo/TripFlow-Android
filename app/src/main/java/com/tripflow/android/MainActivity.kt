package com.tripflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.tripflow.core.ui.theme.TripFlowTheme
import com.tripflow.feature.booking.BookingListScreen
import com.tripflow.feature.booking.BookingScreen
import com.tripflow.feature.review.ReviewListScreen
import com.tripflow.feature.review.WriteReviewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripFlowTheme {
                var currentScreen by remember { mutableStateOf("menu") }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (currentScreen) {
                        "menu" -> MenuScreen(onNavigate = { currentScreen = it })
                        "booking_form" -> BookingScreen(onBack = { currentScreen = "menu" })
                        "booking_list" -> BookingListScreen(onBookingClick = { currentScreen = "menu" })
                        //"review_list" -> ReviewListScreen(onBack = { currentScreen = "menu" }) in arrivo
                        //"write_review" -> WriteReviewScreen(onBack = { currentScreen = "menu" }) in arrivo
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
            //Button(onClick = { onNavigate("review_list") }, modifier = Modifier.fillMaxWidth()) {
                //Text("Review List")
            //}
            //Button(onClick = { onNavigate("write_review") }, modifier = Modifier.fillMaxWidth()) {
                //Text("Write Review")
            //}
        }
    }
}


