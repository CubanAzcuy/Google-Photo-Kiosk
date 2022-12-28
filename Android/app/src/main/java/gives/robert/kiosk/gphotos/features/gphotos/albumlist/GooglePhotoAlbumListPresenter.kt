package gives.robert.kiosk.gphotos.features.gphotos.albumlist

import androidx.compose.runtime.*
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.AlbumInfo
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumUiState
import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleAlbum
import gives.robert.kiosk.gphotos.utils.extensions.toImageLoadingRequest
import gives.robert.kiosk.gphotos.utils.providers.CoilProvider
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences

@Composable
fun GooglePhotoAlbumListPresenter(
    googleGooglePhotoRepo: GooglePhotoRepository,
    localRepo: GooglePhotoAlbumListLocalRepo,
    coilProvider: CoilProvider,
    events: ListPhotoAlbumEvents?
): State<ListPhotoAlbumUiState> {

    val uiState = remember { mutableStateOf(ListPhotoAlbumUiState())}

    LaunchedEffect(events) {
        val listPhotoAlbumEvents = events ?: return@LaunchedEffect
        val updatedUiState: ListPhotoAlbumUiState = when (listPhotoAlbumEvents) {
            ListPhotoAlbumEvents.GetAlbums -> {
                val albumList = googleGooglePhotoRepo.fetchAlbums()
                localRepo.setAlbumList(albumList)
                albumList.forEach {
                    val request = it.toImageLoadingRequest(coilProvider.imageBuilder)
                    coilProvider.imageLoader.enqueue(request)
                }
                googleGooglePhotoRepo.saveSeenAlbumList(albumList)
                ListPhotoAlbumUiState(localRepo.getAlbumInfos(coilProvider))
            }
            is ListPhotoAlbumEvents.SelectAlbum -> {
                localRepo.selectAlbum(listPhotoAlbumEvents.selectedAlbumsId)
                ListPhotoAlbumUiState(localRepo.getAlbumInfos(coilProvider))
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
    private var albumList: List<GoogleAlbum> = emptyList()

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

    fun setAlbumList(albumList: List<GoogleAlbum>) {
        this.albumList = albumList
    }

    fun getAlbumInfos(coilProvider: CoilProvider): List<AlbumInfo> {
        return albumList.map {
            val isSelected = selectedAlbumIds.contains(it.id)
            AlbumInfo(
                url = it.coverPhotoUrl,
                title = it.title,
                id = it.id,
                request = it.toImageLoadingRequest(coilProvider.imageBuilder),
                isSelected = isSelected)
        }
    }
}
