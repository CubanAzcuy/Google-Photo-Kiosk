package gives.robert.kiosk.gphotos.features.gphotos.data

import gives.robert.kiosk.gphotos.Database
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleAlbum
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineGooglePhotosRepository(
    private val database: Database,
) {
    suspend fun fetchAlbums(): List<GoogleAlbum> {
        return withContext(Dispatchers.IO) {
            database.albumsQueries.selectAllCached(true).executeAsList().map {
                GoogleAlbum(it.id, it.cover_photo_url, it.title)
            }
        }
    }

    suspend fun fetchPhotos(): List<GoogleMediaItem> {
        return withContext(Dispatchers.IO) {
            database.photosQueries.selectAllCached(true).executeAsList().map {
                GoogleMediaItem(
                    id = it.id,
                    baseUrl = it.url,
                    mimeType = it.mime_type,
                    albumId = it.album_id,
                    fileName = it.file_name)
            }
        }
    }

    suspend fun updateAlbumList(googleAlbums: List<GoogleAlbum>) {
        withContext(Dispatchers.IO) {
            database.albumsQueries.transaction {
                googleAlbums.forEach {
                    database.albumsQueries.updateDownloadedStatus(
                        id = it.id,
                    )
                }
            }
        }
    }

    suspend fun saveSeenPhoto(mediaItem: GoogleMediaItem) {
        withContext(Dispatchers.IO) {
            database.photosQueries
            database.photosQueries.updateDownloadedStatus(
                id = mediaItem.id,
            )
        }
    }
}
