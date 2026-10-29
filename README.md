# TripFlow — App Android

App Android nativa di **TripFlow**, la piattaforma di viaggi a microservizi. L'app comunica con il backend esclusivamente tramite il Gateway.

Backend: **[tripFlow_app](https://github.com/aletave/tripFlow_app)**

---

## Tecnologie

*   **Kotlin** e **Jetpack Compose** (Material 3)
*   **Hilt** per la dependency injection
*   **Retrofit** + Gson per le chiamate REST
*   **Navigation Compose** e Coroutines
*   **Coil** per le immagini
*   **Stripe Android SDK** (PaymentSheet) per i pagamenti
*   **Security Crypto** per la conservazione cifrata dei token

`minSdk 29`, `targetSdk 36`.

---

## Struttura dei moduli

| Modulo | Contenuto | Autore |
| :--- | :--- | :--- |
| `app` | Entry point, navigazione e bottom bar in base al ruolo | - |
| `core:core-model` | Stato comune delle schermate (`UiState`) | - |
| `core:core-network` | Client Retrofit verso il Gateway, DTO e refresh del token | - |
| `core:core-auth` | Login, logout e salvataggio cifrato dei token | - |
| `core:core-ui` | Tema, palette e componenti grafici comuni | - |
| `feature:feature-auth` | Registrazione, login, verifica email, recupero password, profilo | Alessandro Taverniti |
| `feature:feature-catalog` | Catalogo viaggi e attività, gestione da parte dell'organizzatore | Umberto Messina |
| `feature:feature-booking` | Prenotazioni e pagamento con Stripe | Francesco Molinaro |
| `feature:feature-itinerary` | Itinerari, tappe e condivisione | Mattia Taccone |
| `feature:feature-review` | Recensioni dei viaggi completati | Francesco Molinaro |

---

## Avvio

### 1. Prerequisiti
*   **Android Studio** con un **emulatore** Android (API 29 o superiore)
*   Il **backend avviato** con Docker Compose, come descritto nel [README del backend](https://github.com/aletave/tripFlow_app)

### 2. Esecuzione
1. Aprire la cartella del progetto in Android Studio e attendere la sincronizzazione Gradle.
2. Avviare l'emulatore.
3. Eseguire la configurazione `app`.

Non serve configurare chiavi: la chiave pubblicabile di Stripe arriva dal backend.

### 3. Prove
Nel README del backend, le sezioni **"Pagamenti con Stripe (modalità test)"** e **"Provare l'App Android"** spiegano come:
*   leggere il codice di verifica della registrazione su Mailpit (`http://localhost:8025`);
*   pagare con le carte di test di Stripe;
*   condividere un itinerario con un altro utente;
*   rendere recensibile una prenotazione senza attendere la fine del viaggio.

---

## Autori
Sviluppato come progetto per l'Università della Calabria (UNICAL).
Autori del progetto: Francesco Molinaro, Mattia Taccone, Umberto Messina, Alessandro Taverniti.
