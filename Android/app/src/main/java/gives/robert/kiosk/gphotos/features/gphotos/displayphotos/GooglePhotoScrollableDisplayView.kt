package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.OfflineGooglePhotosRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.OnlineGooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.utils.*
import gives.robert.kiosk.gphotos.utils.extensions.observeConnectivityAsFlow
import gives.robert.kiosk.gphotos.utils.providers.DatabaseQueryProvider
import gives.robert.kiosk.gphotos.utils.providers.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.providers.NavigationManager
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences

@Composable
fun SetupGooglePhotoScrollableView(
    navigationManager: NavigationManager,
    userPrefs: UserPreferences
) {
    val context = LocalContext.current
    val googlePhotoRepo = remember {
        val online = OnlineGooglePhotoRepository(HttpClientProvider.client, userPrefs)
        val offline = OfflineGooglePhotosRepository(DatabaseQueryProvider.getInstance(context).database)
        GooglePhotoRepository(online, offline, context.observeConnectivityAsFlow())
    }
    val presenter = remember {
        GooglePhotoScrollableDisplayPresenter(googleGooglePhotoRepo = googlePhotoRepo)
    }

    presenter.processEvent(DisplayPhotoEvents.GetPhotos)
    GooglePhotoScrollableDisplayView(
        presenter.stateFlow.collectAsState(initial = DisplayPhotosState()),
        onScrolledBack = {
            presenter.processEvent(DisplayPhotoEvents.ScrollingStopped(it))
        },
        onScrolling = {
            presenter.processEvent(DisplayPhotoEvents.Scrolling)
        })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GooglePhotoScrollableDisplayView(
    displayPhotosState: State<DisplayPhotosState>,
    onScrolledBack: (Int) -> Unit,
    onScrolling: () -> Unit
) {
    val scrollViewState = rememberLazyListState()
    val photoKioskState = displayPhotosState.value

    val fullyVisibleIndices = remember {
        derivedStateOf {
            val layoutInfo = scrollViewState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                emptyList()
            } else {
                val fullyVisibleItemsInfo = visibleItemsInfo.toMutableList()

                val lastItem = fullyVisibleItemsInfo.last()

                val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

                if (lastItem.offset + lastItem.size > viewportHeight) {
                    fullyVisibleItemsInfo.removeLast()
                }

                val firstItemIfLeft = fullyVisibleItemsInfo.firstOrNull()
                if (firstItemIfLeft != null && firstItemIfLeft.offset < layoutInfo.viewportStartOffset) {
                    fullyVisibleItemsInfo.removeFirst()
                }

                fullyVisibleItemsInfo.map { it.index }
            }
        }
    }

    if (!scrollViewState.isScrollInProgress) {
        if (fullyVisibleIndices.value.isNotEmpty()) {
            onScrolledBack(fullyVisibleIndices.value.first())
        }
    } else {
        onScrolling()
    }

    LaunchedEffect(key1 = photoKioskState.currentIndex) {
        scrollViewState.scrollToItem(photoKioskState.currentIndex)
    }

    LazyRow(
        state = scrollViewState,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollViewState)
    ) {
        items(photoKioskState.photoUrls) {
            Box(
                modifier = Modifier
                    .fillParentMaxSize()
                    .background(color = Color.Black),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = it.baseUrl,
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
}
