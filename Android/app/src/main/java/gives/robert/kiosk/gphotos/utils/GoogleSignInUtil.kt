package gives.robert.kiosk.gphotos.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

class GoogleSignInUtil(context: Context, private val clientId: String) {

    private val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions())

    val requestServerAuthTokenIntent
        get() = googleSignInClient.signInIntent

    private fun googleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestServerAuthCode(
                clientId,
                false
            )
            .requestScopes(photoLibPermissionScope)
            .build()
    }

    companion object {
        val photoLibPermissionScope = Scope("https://www.googleapis.com/auth/photoslibrary.readonly")
        const val attemptSignInRequestCode = 25

    }
}