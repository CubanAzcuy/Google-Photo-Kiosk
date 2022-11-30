package gives.robert.kiosk.gphotos.features.gphotos.albumlist

import androidx.compose.runtime.*
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.AlbumInfo
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumUiState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.networking.models.Albums
import gives.robert.kiosk.gphotos.utils.UserPreferences

@Composable
fun GooglePhotoAlbumListPresenter(
    googleGooglePhotoRepo: GooglePhotoRepository,
    localRepo: GooglePhotoAlbumListLocalRepo,
    events: ListPhotoAlbumEvents?
): State<ListPhotoAlbumUiState> {

    val uiState = remember { mutableStateOf(ListPhotoAlbumUiState())}

    LaunchedEffect(events) {
        val listPhotoAlbumEvents = events ?: return@LaunchedEffect
        val updatedUiState: ListPhotoAlbumUiState = when (listPhotoAlbumEvents) {
            ListPhotoAlbumEvents.GetAlbums -> {
                val albumList = googleGooglePhotoRepo.getAlbums()
                localRepo.setAlbumList(albumList)
                ListPhotoAlbumUiState(localRepo.getAlbumInfos())
            }
            is ListPhotoAlbumEvents.SelectAlbum -> {
                localRepo.selectAlbum(listPhotoAlbumEvents.selectedAlbumsId)
                ListPhotoAlbumUiState(localRepo.getAlbumInfos())
            }
            else -> {
                uiState.value
            }
        }

        uiState.value = updatedUiState
    }

    return uiState
}

class GooglePhotoAlbumListLocalRepo(
    private val userPrefs: UserPreferences,
) {
    private val selectedAlbumIds = mutableSetOf<String>()
    private var albumList: List<Albums> = emptyList()

    init {
        selectedAlbumIds.addAll(userPrefs.userPreferencesRecord.selectedAlbumIds)
    }

    suspend fun selectAlbum(id: String) {
        if (selectedAlbumIds.contains(id)) {
            selectedAlbumIds.remove(id)
        } else {
            selectedAlbumIds.add(id)
        }

        userPrefs.setSelectedAlbums(selectedAlbumIds)
    }

    fun setAlbumList(albumList: List<Albums>) {
        this.albumList = albumList
    }

    fun getAlbumInfos(): List<AlbumInfo> {
        return albumList.map {
            val isSelected = selectedAlbumIds.contains(it.id)
            AlbumInfo(it.coverPhotoBaseUrl, it.title, it.id, isSelected)
        }
    }
}
