package gives.robert.kiosk.gphotos.features

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.Coil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import gives.robert.kiosk.gphotos.BuildConfig
import gives.robert.kiosk.gphotos.features.config.ConfigPresenter
import gives.robert.kiosk.gphotos.features.config.data.AuthRepository
import gives.robert.kiosk.gphotos.features.config.ui.ConfigView
import gives.robert.kiosk.gphotos.features.config.ui.io.ConfigureKioskEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.SetupGooglePhotoAlbumsSelectorView
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.SetupGooglePhotoScrollableView
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.utils.*
import gives.robert.kiosk.gphotos.utils.extensions.ConnectionState
import gives.robert.kiosk.gphotos.utils.extensions.connectivityState
import gives.robert.kiosk.gphotos.utils.providers.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


class MainActivity : ComponentActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var configPresenter: ConfigPresenter
//    fun getRowCount(): Long {
//        return DatabaseUtils.queryNumEntries(readableDatabase, TABLE_NAME)
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        asdfasdF()
        val userPrefs = UserPreferences.getInstance(applicationContext)
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
                    LocalView.current.keepScreenOn = true

                    val context = LocalContext.current
                    val prefs = userPrefs.preferencesFlow.collectAsState().value

                    val navigationManager = remember {
                        val startThing = prefs.selectedAlbumIds.isEmpty()
                        NavigationManager(startThing)
                    }
                    val connection by connectivityState()
                    val photoList = remember {
                        DatabaseQueryProvider.getInstance(context).photoList()
                    }
                    val photoListState = photoList.collectAsState(initial = emptyList())

                    val runInOfflineMode = connection != ConnectionState.Available && photoListState.value.isNotEmpty()

                    when {
                        runInOfflineMode -> {
                            SetupGooglePhotoScrollableView(navigationManager, userPrefs)
                        }

                        connection != ConnectionState.Available -> {
                            LaunchedEffect(LocalLifecycleOwner.current.lifecycle.currentState) {
                                startActivity(Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"))
                            }
                        }
                        prefs?.authToken == null -> {
                            requestServerAuthToken(googleSignInUtil.requestServerAuthTokenIntent)
                            ConfigView()
                        }
                        else -> {
                            mainNavigation(navigationManager, userPrefs)
                        }
                    }
                }
            }
        }
    }

    private fun asdfasdF() {

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } else {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            // Apply the screen brightness value to the system, this will change
            // the value in Settings ---> Display ---> Brightness level.
            // It will also change the screen brightness for the device.

            val hexValue = getValueFromPercent(68.5)
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                hexValue
            )

        }

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

    }

    private fun getValueFromPercent(value: Double): Int = ((value/100) * 255).toInt()

    @Composable
    private fun mainNavigation(navigationManager: NavigationManager, userPrefs: UserPreferences) {
        when (navigationManager.currentLocationFlow.collectAsState().value) {
            NavigationLocations.PHOTOS_DISPLAY -> {
                SetupGooglePhotoScrollableView(navigationManager, userPrefs)
            }
            NavigationLocations.ALBUM_SELECT -> {
                SetupGooglePhotoAlbumsSelectorView(navigationManager, userPrefs)
            }
        }
    }

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
