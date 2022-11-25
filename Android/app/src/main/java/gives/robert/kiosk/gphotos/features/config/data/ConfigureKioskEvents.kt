package gives.robert.kiosk.gphotos.features.config.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface ConfigureKioskEvents {
    data class TokenFetched(val googleAccount: GoogleSignInAccount) : ConfigureKioskEvents
}