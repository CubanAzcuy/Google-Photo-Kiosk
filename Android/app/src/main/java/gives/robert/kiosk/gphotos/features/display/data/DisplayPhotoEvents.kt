package gives.robert.kiosk.gphotos.features.display.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface DisplayPhotoEvents {
    object GetPhotos: DisplayPhotoEvents
}