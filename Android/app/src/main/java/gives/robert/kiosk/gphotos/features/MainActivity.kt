package gives.robert.kiosk.gphotos.features

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import gives.robert.kiosk.gphotos.BuildConfig
import gives.robert.kiosk.gphotos.features.config.ConfigPresenter
import gives.robert.kiosk.gphotos.features.config.data.AuthRepository
import gives.robert.kiosk.gphotos.features.config.ui.ConfigView
import gives.robert.kiosk.gphotos.features.config.ui.io.ConfigureKioskEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.SetupGooglePhotoAlbumsSelectorView
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.SetupGooglePhotoScrollableView
import gives.robert.kiosk.gphotos.features.wifi.WifiView
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.utils.*
import gives.robert.kiosk.gphotos.utils.extensions.ConnectionState
import gives.robert.kiosk.gphotos.utils.extensions.connectivityState
import gives.robert.kiosk.gphotos.utils.providers.*

class MainActivity : ComponentActivity() {

    private lateinit var configPresenter: ConfigPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestFullScreen()
        val userPrefs = UserPreferences.getInstance(applicationContext)
        configPresenter = ConfigPresenter(
            AuthRepository(
                BuildConfig.AUTH_SERVER_IP,
                userPrefs,
                HttpClientProvider.client
            )
        )
        val googleSignInUtil = GoogleSignInUtil(application, BuildConfig.GOOGLE_OAUTH_CLIENT_ID)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocalView.current.keepScreenOn = true
                    val context = LocalContext.current
                    val userPrefsState = userPrefs.preferencesFlow.collectAsState()
                    val requestedLocations = remember {
                        NavigationManager()
                    }
                    val connectionState by connectivityState()

                    val navigationLocations =
                        SetupNavigation(context, connectionState, userPrefsState, requestedLocations).value
                    if (navigationLocations == NavigationLocations.DEFAULT) return@Surface

                    when (navigationLocations) {
                        NavigationLocations.ALBUM_SELECT -> {
                            SetupGooglePhotoAlbumsSelectorView(requestedLocations, userPrefs)
                        }
                        NavigationLocations.SETUP_WIFI -> {
                            WifiView()
                        }
                        NavigationLocations.FIRST_AUTH -> {
                            ConfigView {
                                requestServerAuthToken(googleSignInUtil.requestServerAuthTokenIntent)
                            }
                        }
                        else -> {
                            SetupGooglePhotoScrollableView(requestedLocations, userPrefs)
                        }
                    }

                    if (navigationLocations != NavigationLocations.FIRST_AUTH &&
                        userPrefsState.value.authToken == null &&
                        (connectionState == ConnectionState.Available && navigationLocations != NavigationLocations.SETUP_WIFI)) {
                        requestServerAuthToken(googleSignInUtil.requestServerAuthTokenIntent)
                    }
                }
            }
        }
    }

    private fun requestFullScreen() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setBrightness() {
        //        if (!Settings.System.canWrite(this)) {
//            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
//            intent.data = Uri.parse("package:$packageName")
//            startActivity(intent)
//        } else {
//            Settings.System.putInt(
//                contentResolver,
//                Settings.System.SCREEN_BRIGHTNESS_MODE,
//                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
//            )
//            // Apply the screen brightness value to the system, this will change
//            // the value in Settings ---> Display ---> Brightness level.
//            // It will also change the screen brightness for the device.
//
//            val hexValue = getValueFromPercent(68.5)
//            Settings.System.putInt(
//                contentResolver,
//                Settings.System.SCREEN_BRIGHTNESS,
//                hexValue
//            )
//
//        }
    }

    private fun getValueFromPercent(value: Double): Int = ((value / 100) * 255).toInt()

    private fun requestServerAuthToken(requestServerAuthTokenIntent: Intent) {
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
}
