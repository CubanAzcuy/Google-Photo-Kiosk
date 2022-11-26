package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import gives.robert.kiosk.gphotos.BuildConfig
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.utils.BetterRandom
import gives.robert.kiosk.gphotos.utils.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.UserPreferences

@Composable
fun SetupGooglePhotoScrollableView() {
    val application = LocalContext.current.applicationContext

    val presenter = remember {
        GooglePhotoScrollableDisplayPresenter(
            googleGooglePhotoRepo = GooglePhotoRepository(
                HttpClientProvider.client,
                UserPreferences(application)
            ),
            testAlbum = BuildConfig.TEST_ALBUM
        )
    }

    presenter.processEvent(DisplayPhotoEvents.GetPhotos)
    GooglePhotoScrollableDisplayView(presenter.stateFlow.collectAsState(initial = DisplayPhotosState()))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GooglePhotoScrollableDisplayView(displayPhotosState: State<DisplayPhotosState>) {
    val scrollViewState = rememberLazyListState()
    val betterRandom = remember {
        BetterRandom()
    }
    val photoKioskState = displayPhotosState.value

    LaunchedEffect(key1 = photoKioskState.currentIndex) {
        scrollViewState.scrollToItem(photoKioskState.currentIndex)
    }

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
                        val index = betterRandom.nextRandom(0 until photoKioskState.photoUrls.size)
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
