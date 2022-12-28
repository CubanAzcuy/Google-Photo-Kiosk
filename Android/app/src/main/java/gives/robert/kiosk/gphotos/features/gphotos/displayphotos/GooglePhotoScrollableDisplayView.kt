package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
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
import coil.Coil
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.OfflineGooglePhotosRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.OnlineGooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosUiState
import gives.robert.kiosk.gphotos.utils.*
import gives.robert.kiosk.gphotos.utils.extensions.defaultImageLoader
import gives.robert.kiosk.gphotos.utils.extensions.observeConnectivityAsFlow
import gives.robert.kiosk.gphotos.utils.providers.*

@Composable
fun SetupGooglePhotoScrollableView(
    navigationManager: NavigationManager,
    userPrefs: UserPreferences
) {
    val context = LocalContext.current
    Coil.setImageLoader(context.defaultImageLoader())

    val coilProvider = remember {
        CoilProvider.getInstance(context)
    }

    val googlePhotoRepo = remember {
        val online = OnlineGooglePhotoRepository(HttpClientProvider.client, userPrefs, DatabaseQueryProvider.getInstance(context).database)
        val offline = OfflineGooglePhotosRepository(DatabaseQueryProvider.getInstance(context).database)
        GooglePhotoRepository(online, offline, context.observeConnectivityAsFlow())
    }

    val presenter = remember {
        GooglePhotoScrollableDisplayPresenter(
            userPrefs = userPrefs,
            googleGooglePhotoRepo = googlePhotoRepo,
            coilProvider = coilProvider
        )
    }

    presenter.processEvent(DisplayPhotoEvents.GetPhotos)
    val radsfasdf = presenter.stateFlow.collectAsState(initial = DisplayPhotosUiState())
    GooglePhotoScrollableDisplayView(
        radsfasdf,
        onScrolledBack = {
            presenter.processEvent(DisplayPhotoEvents.ScrollingStopped(it))
        },
        onScrolling = {
            presenter.processEvent(DisplayPhotoEvents.Scrolling)
        },
        onAuthLost = {
            presenter.processEvent(DisplayPhotoEvents.OnAuthLost)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GooglePhotoScrollableDisplayView(
    displayPhotosState: State<DisplayPhotosUiState>,
    onScrolledBack: (Int) -> Unit,
    onScrolling: () -> Unit,
    onAuthLost: () -> Unit,
) {
    val scrollViewState = rememberLazyListState()
    val photoKioskState = displayPhotosState.value

    val fullyVisibleIndices = getVisibleImagesForListView(scrollViewState)

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
                    model = it.request,
                    modifier = Modifier.fillParentMaxSize(),
                    loading = {
                        CircularProgressIndicator()
                    },
                    onSuccess = {
                        val asdf = ""
                    },
                    onError = {
                        onAuthLost()
                    },
                    contentDescription = "stringResource(R.string.description)"
                )
            }
        }
    }
}

@Composable
private fun getVisibleImagesForListView(scrollViewState: LazyListState): State<List<Int>> {
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
    return fullyVisibleIndices
}
