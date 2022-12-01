package gives.robert.kiosk.gphotos.features.gphotos.data

import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleAlbum
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import gives.robert.kiosk.gphotos.utils.extensions.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GooglePhotoRepository(
    private val onlineGooglePhotoRepository: OnlineGooglePhotoRepository,
    private val offlineGooglePhotoRepository: OfflineGooglePhotosRepository,
    connectionStateFlow: Flow<ConnectionState>
) {

    private var connectionState: ConnectionState = ConnectionState.Available
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            connectionStateFlow.collect { state ->
                connectionState = state
            }
        }
    }

    suspend fun fetchAlbums(): List<GoogleAlbum> {
        return if (connectionState == ConnectionState.Available) {
            val result = onlineGooglePhotoRepository.fetchAlbums()
            saveSeenAlbums(result)
            return result
        } else {
            offlineGooglePhotoRepository.fetchAlbums()
        }
    }

    suspend fun fetchPhotos(): List<GoogleMediaItem> {
        return if (connectionState == ConnectionState.Available) {
            onlineGooglePhotoRepository.fetchPhotos()
        } else {
            offlineGooglePhotoRepository.fetchPhotos()
        }
    }

    private suspend fun saveSeenAlbums(googleAlbums: List<GoogleAlbum>) {
        offlineGooglePhotoRepository.saveSeenAlbums(googleAlbums)
    }

    suspend fun saveSeenPhoto(mediaItem: GoogleMediaItem) {
        offlineGooglePhotoRepository.saveSeenPhoto(mediaItem)
    }
}