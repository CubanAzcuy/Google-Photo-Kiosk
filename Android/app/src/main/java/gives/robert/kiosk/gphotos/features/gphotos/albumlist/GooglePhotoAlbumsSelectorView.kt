package gives.robert.kiosk.gphotos.features.gphotos.albumlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import coil.compose.SubcomposeAsyncImage
import gives.robert.kiosk.gphotos.R
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
        selectAlbum = {
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

    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
        constraintSet = decoupledConstraints()
    ) {

        Box(modifier = Modifier.layoutId("photos")) {
            Dia(listPhotoAlbumStateState, selectAlbum)
        }

        Button(
            modifier = Modifier
                .layoutId("button")
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Magenta),
            shape = RectangleShape,
            contentPadding = PaddingValues(0.dp),
            onClick = doneWithSelections
        ) {
            Text(
                text = "BATMAN"
            )
        }
    }
}

private fun decoupledConstraints(): ConstraintSet {
    return ConstraintSet {
        val photoAlbumView = createRefFor("photos")
        val button = createRefFor("button")

        constrain(button) {
            end.linkTo(parent.end)
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom)
        }

        constrain(photoAlbumView) {
            top.linkTo(parent.top)
            end.linkTo(parent.end)
            start.linkTo(parent.start)
            bottom.linkTo(button.top)
        }
    }
}

@Composable
private fun Dia(
    listPhotoAlbumStateState: State<ListPhotoAlbumState>,
    selectAlbum: (String) -> Unit
) {
    val scrollViewState = rememberLazyGridState()
    val listPhotoAlbumState = listPhotoAlbumStateState.value
    LazyVerticalGrid(
        state = scrollViewState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        columns = GridCells.Adaptive(minSize = 128.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(listPhotoAlbumState.albums, itemContent = { item ->
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(128.dp)
                    .background(Color.Black)
                    .clickable {
                        selectAlbum(item.id)
                    }
            ) {
                SubcomposeAsyncImage(
                    model = item.url,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        CircularProgressIndicator()
                    },
                    onError = {
                        val sdfasdfsda = ""
                    },
                    contentDescription = "stringResource(R.string.description)"
                )

                if (item.isSelected) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopEnd)
                            .width(34.dp)
                            .height(34.dp)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                }

                Text(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(color = Color.White.copy(alpha = 0.33f)),
                    text = item.title,
                )
            }
        })
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