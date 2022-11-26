package gives.robert.kiosk.gphotos.features.gphotos.albumlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.AlbumInfo
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.utils.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.NavigationLocations
import gives.robert.kiosk.gphotos.utils.NavigationManager
import gives.robert.kiosk.gphotos.utils.UserPreferences
import kotlinx.coroutines.flow.flowOf

@Composable
fun SetupGooglePhotoAlbumsSelectorView(navigationManager: NavigationManager) {
    val application = LocalContext.current.applicationContext
    val userPrefs = UserPreferences(application)

    val presenter = remember {
        GooglePhotoAlbumListPresenter(
            googleGooglePhotoRepo = GooglePhotoRepository(
                HttpClientProvider.client,
                userPrefs
            ),
            localRepo = GooglePhotoAlbumListLocalRepo(userPrefs)
        )
    }

    presenter.processEvent(ListPhotoAlbumEvents.GetAlbums)
    GooglePhotoAlbumsSelectorView(presenter.stateFlow.collectAsState(initial = ListPhotoAlbumState()),
        selectAlbum = { it: String ->
            presenter.processEvent(ListPhotoAlbumEvents.SelectAlbum(it))
        }, doneWithSelections = {
            navigationManager.gotoLocation(NavigationLocations.PHOTOS_DISPLAY)
        })
}

@Composable
fun GooglePhotoAlbumsSelectorView(
    listPhotoAlbumStateState: State<ListPhotoAlbumState>,
    selectAlbum: (String) -> Unit,
    doneWithSelections: () -> Unit,
) {
    val scrollViewState = rememberLazyListState()
    val listPhotoAlbumState = listPhotoAlbumStateState.value

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollViewState,
            modifier = Modifier.fillMaxSize().background(Color.Green),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(listPhotoAlbumState.albums, itemContent = { item ->
                Button(
                    modifier = Modifier.background(Color.Red),
                    onClick = {
                        selectAlbum(item.id)
                    }) {
                    val selectedColor = if (item.isSelected) Color.Black else Color.Gray
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(selectedColor)
                    ) {
                        SubcomposeAsyncImage(
                            model = item.url,
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp),
                            loading = {
                                CircularProgressIndicator()
                            },
                            onError = {
                                val sdfasdfsda = ""
                            },
                            contentDescription = "stringResource(R.string.description)"
                        )
                        Text(text = item.title)
                    }
                }
            })
        }

        Button(
            modifier = Modifier
                .fillMaxWidth(.9f)
                .height(300.dp)
                .background(Color.Magenta),
            onClick = doneWithSelections
        ) {
            Text(text = "BATMAN")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        val albumsInfos = listOf(
            AlbumInfo(
                "https://images.unsplash.com/photo-1554080353-a576cf803bda?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8OXx8cGhvdG9ncmFwaHl8ZW58MHx8MHx8&w=1000&q=80",
                "Name",
                "id",
                true
            ),
            AlbumInfo(
                "https://images.unsplash.com/photo-1554080353-a576cf803bda?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8OXx8cGhvdG9ncmFwaHl8ZW58MHx8MHx8&w=1000&q=80",
                "Name2",
                "id2",
                false
            )

        )
        val flow = flowOf(ListPhotoAlbumState(albumsInfos))
        GooglePhotoAlbumsSelectorView(flow.collectAsState(initial = ListPhotoAlbumState()),
            selectAlbum = {

            }, doneWithSelections = {

            })
    }
}