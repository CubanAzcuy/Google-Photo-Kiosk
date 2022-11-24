package gives.robert.kiosk.gphotos.features.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface PhotoKioskEvents {
    data class TokenFetched(val googleAccount: GoogleSignInAccount) : PhotoKioskEvents
    object GetPhotos: PhotoKioskEvents
}