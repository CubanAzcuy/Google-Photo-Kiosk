package gives.robert.kiosk.gphotos

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.ListAlbumsPagedResponse
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.SearchMediaItemsPagedResponse
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.ui.theme.PhotosLibraryClientFactory

//TODO: https://github.com/google/java-photoslibrary/blob/main/sample/src/main/java/com/google/photos/library/sample/factories/PhotosLibraryClientFactory.java
//https://github.com/googleapis/google-api-nodejs-client/issues/1951

class MainActivity : ComponentActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    val scopedURL = "https://www.googleapis.com/auth/photoslibrary.readonly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherThings()
        setContent {
            MyApplicationTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GooglePhotoView()
                }
            }
        }
    }

    private fun otherThings() {
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions())
        signIn()
    }

    private fun googleSignInOptions(): GoogleSignInOptions {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestServerAuthCode(
                "",
                false
            )
            .requestScopes(Scope(scopedURL))
            .build()
        return gso
    }


    private fun signIn() {
        // Launches the sign in flow, the result is returned in onActivityResult
        val intent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, 25)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                // Sign in succeeded, proceed with account
                val acct: GoogleSignInAccount = task.result
                val photosLibraryClient = PhotosLibraryClientFactory.createClient(acct)!!
                listAlbums(photosLibraryClient)
            } else {
                // Sign in failed, handle failure and update UI
                // ...
            }
        }
    }

    private fun listAlbums(it: PhotosLibraryClient) {
        try {
            // Make a request to list all albums in the user's library
            // Iterate over all the albums in this list
            // Pagination is handled automatically
            val response: ListAlbumsPagedResponse = it.listAlbums()
            for (album in response.iterateAll()) {
                // Get some properties of an album
                val id = album.id
                val title = album.title
                val productUrl = album.productUrl
                val coverPhotoBaseUrl = album.coverPhotoBaseUrl
                // The cover photo media item id field may be empty
                val coverPhotoMediaItemId = album.coverPhotoMediaItemId
                val isWritable = album.isWriteable
                val mediaItemsCount = album.mediaItemsCount

                val response: SearchMediaItemsPagedResponse = it.searchMediaItems(album.id)
                for (item in response.iterateAll()) {
                    val adfasdf = item.productUrl;
                }
            }
        } catch (e: Exception) {
            // Handle error
        }

    }

    fun checkPermission() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        // Synchronously check for necessary permissions

        // Synchronously check for necessary permissions
        if (!GoogleSignIn.hasPermissions(account, Scope(scopedURL))) {
            GoogleSignIn.requestPermissions(this, 33, account, Scope(scopedURL))
            return
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setFullScreen()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
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
fun GooglePhotoView() {
    val state = rememberLazyListState()

    LazyRow(
        state = state,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
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
                    SubcomposeAsyncImage(
                        model = "https://www.computerhope.com/jargon/u/url.png",
                        modifier = Modifier.fillParentMaxSize(),
                        loading = {
                            CircularProgressIndicator()
                        },
                        contentDescription = "stringResource(R.string.description)"
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GooglePhotoView()
    }
}
