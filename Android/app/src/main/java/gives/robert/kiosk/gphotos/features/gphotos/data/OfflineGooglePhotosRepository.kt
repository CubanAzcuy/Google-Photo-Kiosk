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
            database.seenAlbumsQueries.selectAll().executeAsList().map {
                GoogleAlbum(it.id, it.cover_photo_url, it.title)
            }
        }
    }

    suspend fun fetchPhotos(): List<GoogleMediaItem> {
        return withContext(Dispatchers.IO) {
            database.seenPhotosQueries.selectAll().executeAsList().map {
                GoogleMediaItem(it.id, it.url, it.mime_type, it.album_id,)
            }
        }
    }

    suspend fun saveSeenAlbums(googleAlbums: List<GoogleAlbum>) {
        withContext(Dispatchers.IO) {
            database.seenAlbumsQueries.transaction {
                googleAlbums.forEach {
                    database.seenAlbumsQueries.insert(
                        id = it.id,
                        title = it.title,
                        cover_photo_url = it.coverPhotoUrl
                    )
                }
            }
        }
    }

    suspend fun saveSeenPhoto(mediaItem: GoogleMediaItem) {
        withContext(Dispatchers.IO) {
            database.seenPhotosQueries.insert(
                id = mediaItem.id,
                album_id = mediaItem.albumId,
                url = mediaItem.baseUrl,
                mime_type = mediaItem.mimeType
            )
        }
    }
}
