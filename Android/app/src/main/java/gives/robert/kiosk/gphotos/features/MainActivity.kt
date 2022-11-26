package gives.robert.kiosk.gphotos.features

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import coil.Coil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import gives.robert.kiosk.gphotos.BuildConfig
import gives.robert.kiosk.gphotos.features.config.ConfigPresenter
import gives.robert.kiosk.gphotos.features.config.ConfigView
import gives.robert.kiosk.gphotos.features.config.data.ConfigureKioskEvents
import gives.robert.kiosk.gphotos.features.config.networking.AuthRepository
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.SetupGooglePhotoScrollableView
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.SetupGooglePhotoAlbumsSelectorView
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.utils.GoogleSignInUtil
import gives.robert.kiosk.gphotos.utils.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.ImageLoaderProvider
import gives.robert.kiosk.gphotos.utils.UserPreferences

class MainActivity : ComponentActivity(), GoogleApiClient.OnConnectionFailedListener {


    private lateinit var configPresenter: ConfigPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPrefs = UserPreferences(context = applicationContext)
        configPresenter = ConfigPresenter(
            AuthRepository(
                BuildConfig.AUTH_SERVER_IP,
                userPrefs,
                HttpClientProvider.client
            )
        )
        val googleSignInUtil = GoogleSignInUtil(application, BuildConfig.GOOGLE_OAUTH_CLIENT_ID)

        val imageLoader = ImageLoaderProvider.newImageLoader(this)
        Coil.setImageLoader(imageLoader)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prefs = userPrefs.preferencesFlow.collectAsState(initial = null).value
                    if (prefs?.authToken == null) {
                        requestServerAuthToken(googleSignInUtil.requestServerAuthTokenIntent)
                        ConfigView()
                    } else if(prefs.selectedAlbumIds.isEmpty()) {
                        SetupGooglePhotoAlbumsSelectorView()
                    } else {
                        SetupGooglePhotoScrollableView()
                    }
                }
            }
        }
    }

    fun requestServerAuthToken(requestServerAuthTokenIntent: Intent) {
        startActivityForResult(
            requestServerAuthTokenIntent,
            GoogleSignInUtil.attemptSignInRequestCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleSignInUtil.attemptSignInRequestCode) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                configPresenter.processEvent(ConfigureKioskEvents.TokenFetched(task.result))
            } else {
                //TODO:
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setFullScreen()
    }

    private fun setFullScreen() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}
