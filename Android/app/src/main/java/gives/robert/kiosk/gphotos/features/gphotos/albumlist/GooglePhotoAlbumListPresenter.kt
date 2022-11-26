package gives.robert.kiosk.gphotos.features.gphotos.albumlist

import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.AlbumInfo
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumEffect
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumEvents
import gives.robert.kiosk.gphotos.features.gphotos.albumlist.data.ListPhotoAlbumState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.networking.models.Albums
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.UserPreferences
import kotlinx.coroutines.flow.update

class GooglePhotoAlbumListPresenter(
    private val googleGooglePhotoRepo: GooglePhotoRepository,
    private val localRepo: GooglePhotoAlbumListLocalRepo,
) : BasePresenter<ListPhotoAlbumEvents, ListPhotoAlbumState, ListPhotoAlbumEffect>() {

    override val baseState: ListPhotoAlbumState
        get() = ListPhotoAlbumState()

    override suspend fun handleEvent(event: ListPhotoAlbumEvents) {
        when (event) {
            ListPhotoAlbumEvents.GetAlbums -> {
                buildAlbumList()
            }
            is ListPhotoAlbumEvents.SelectAlbum -> {
                selectAlbum(event.selectedAlbumsId)
            }
        }
    }

    private suspend fun selectAlbum(selectedAlbumsId: String) {
        localRepo.selectAlbum(selectedAlbumsId)
        val albums = localRepo.albumList.map {
            val isSelected = localRepo.selectedAlbumIds.contains(it.id)
            AlbumInfo(it.coverPhotoBaseUrl, it.title, it.id, isSelected)
        }

        stateFlow.update {
            it.copy(albums = albums)
        }
    }

    private suspend fun buildAlbumList() {
        val albumList = googleGooglePhotoRepo.getAlbums()
        localRepo.setAlbumList(albumList)

        val albums = albumList.map {
            val isSelected = localRepo.selectedAlbumIds.contains(it.id)
            AlbumInfo(it.coverPhotoBaseUrl, it.title, it.id, isSelected)
        }

        stateFlow.update {
            it.copy(albums = albums)
        }
    }
}

class GooglePhotoAlbumListLocalRepo(
    private val userPrefs: UserPreferences,
) {
    val selectedAlbumIds = mutableSetOf<String>()
    var albumList: List<Albums> = emptyList()
        private set

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
}
