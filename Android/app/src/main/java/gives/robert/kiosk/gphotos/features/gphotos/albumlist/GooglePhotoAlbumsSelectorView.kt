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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.utils.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.UserPreferences

@Composable
fun SetupGooglePhotoAlbumsSelectorView() {
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
    GooglePhotoAlbumsSelectorView(presenter.stateFlow.collectAsState(initial = ListPhotoAlbumState())) { it: String ->
        presenter.processEvent(ListPhotoAlbumEvents.SelectAlbum(it))
    }
}

@Composable
fun GooglePhotoAlbumsSelectorView(
    listPhotoAlbumStateState: State<ListPhotoAlbumState>,
    onClick: (String) -> Unit
) {
    val scrollViewState = rememberLazyListState()
    val listPhotoAlbumState = listPhotoAlbumStateState.value

    LazyColumn(
        state = scrollViewState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(listPhotoAlbumState.albums, itemContent = { item ->
            Button(onClick = {
                onClick(item.id)
            }) {
                val selectedColor = if(item.isSelected) Color.Black else Color.Gray
                Row(modifier = Modifier.fillMaxSize()
                    .background(selectedColor)) {
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

}