package gives.robert.kiosk.gphotos.features

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import gives.robert.kiosk.gphotos.BuildConfig
import gives.robert.kiosk.gphotos.features.data.PhotoKioskEvents
import gives.robert.kiosk.gphotos.features.data.PhotoKioskState
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.utils.GoogleSignInUtil
import gives.robert.kiosk.gphotos.utils.ImageLoaderProvider

class MainActivity : ComponentActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var googleSignInUtil: GoogleSignInUtil
    private lateinit var presenter: Presenter
    private lateinit var imageLoader: ImageLoader


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLoader = ImageLoaderProvider.newImageLoader(this)
        Coil.setImageLoader(imageLoader)

        presenter = Presenter(BuildConfig.AUTH_SERVER_IP, BuildConfig.TEST_ALBUM)
        googleSignInUtil = GoogleSignInUtil(this, BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
        requestServerAuthToken(googleSignInUtil.requestServerAuthTokenIntent)
        observeEffects()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state = presenter.stateFlow.collectAsState()
                    GooglePhotoView(state)
                }
            }
        }
    }

    private fun observeEffects() {
        lifecycleScope.launchWhenStarted {
            presenter.effectFlow.collect { effect ->
                when (effect) {
                    else -> {

                    }
                }
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
                presenter.processEvent(PhotoKioskEvents.TokenFetched(task.result))
            } else {
                //TODO:
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setFullScreen()
    }

    override fun onResume() {
        super.onResume()
//        presenter.processEvent(PhotoKioskEvents.GetPhotos)
//        checkLocation()
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

    private fun checkLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GooglePhotoView(photoKioskStateHolder: State<PhotoKioskState>) {
    val scrollViewState = rememberLazyListState()

    val photoKioskState = photoKioskStateHolder.value

    LazyRow(
        state = scrollViewState,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollViewState)
    ) {
        items(
            count = Int.MAX_VALUE,
            itemContent = {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .background(color = Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoKioskState.photoUrls.isNotEmpty()) {
                        val index = (0 until photoKioskState.photoUrls.size).random()
                        val url = photoKioskState.photoUrls[index].first

                        SubcomposeAsyncImage(
                            model = url,
                            modifier = Modifier.fillParentMaxSize(),
                            loading = {
                                CircularProgressIndicator()
                            },
                            onError = {
                                val sdfasdfsda = ""
                            },
                            contentDescription = "stringResource(R.string.description)"
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GooglePhotoView(null!!)
    }
}
