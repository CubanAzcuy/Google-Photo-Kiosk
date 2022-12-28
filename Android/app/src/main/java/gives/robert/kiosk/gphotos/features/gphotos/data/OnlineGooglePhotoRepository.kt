package gives.robert.kiosk.gphotos.features.gphotos.data

import gives.robert.kiosk.gphotos.Database
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleAlbum
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import gives.robert.kiosk.gphotos.features.gphotos.data.models.wt.AlbumListResponse
import gives.robert.kiosk.gphotos.features.gphotos.data.models.wt.MediaItemSearchRequest
import gives.robert.kiosk.gphotos.features.gphotos.data.models.wt.MediaItemSearchResponseWT
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnlineGooglePhotoRepository(
    private val client: HttpClient,
    private val userPrefs: UserPreferences,
    private val database: Database
) {

    private val bearerToken
        get() = "Bearer ${userPrefs.userPreferencesRecord.authToken}"

    suspend fun fetchPhotos(): List<GoogleMediaItem> {
        return fetchPhotos(userPrefs.userPreferencesRecord.selectedAlbumIds)
    }

    private suspend fun fetchPhotos(albumIds: Set<String>): List<GoogleMediaItem> {

        val cachedRecords = database.photosQueries.selectAll().executeAsList()
        if(!userPrefs.userPreferencesRecord.shouldFetch && cachedRecords.isNotEmpty()) {
            return cachedRecords.map {
                GoogleMediaItem(
                    id = it.id,
                    baseUrl = it.url,
                    mimeType = it.mime_type,
                    albumId = it.album_id,
                    fileName = it.file_name
                )
            }
        }

        val mediaList = mutableListOf<GoogleMediaItem>()
        albumIds.forEach { albumId ->
            try {
                var mediaItemResponse = getMediaItems(albumId)
                mediaList.addAll(googleMediaItemsWTtoGoogleMediaItems(mediaItemResponse, albumId))

                while (mediaItemResponse.nextPageToken != null) {
                    mediaItemResponse = getMediaItems(albumId, mediaItemResponse.nextPageToken)
                    mediaList.addAll(googleMediaItemsWTtoGoogleMediaItems(mediaItemResponse, albumId))
                }
            } catch (ex: Exception) {
                val ex = ex
            }
        }

        database.transaction {
            mediaList.forEach {
                database.photosQueries.insert(
                    id = it.id,
                    album_id = it.albumId,
                    url = it.baseUrl,
                    file_name = it.fileName,
                    mime_type = it.mimeType,
                    downloaded = false
                )
            }
        }

        return mediaList
    }

    private fun googleMediaItemsWTtoGoogleMediaItems(
        mediaItemResponse: MediaItemSearchResponseWT,
        albumId: String
    ) = mediaItemResponse.mediaItems.filter {
        it.baseUrl != null
    }.map { googleMediaItem ->
        GoogleMediaItem(
            id = googleMediaItem.id,
            baseUrl = "${googleMediaItem.baseUrl}=d",
            mimeType = googleMediaItem.mimeType.replace("image/", ""),
            albumId = albumId,
            fileName = googleMediaItem.filename
        )
    }

    suspend fun fetchAlbums(): List<GoogleAlbum> {
        return withContext(Dispatchers.IO) {
            val cachedRecords = database.albumsQueries.selectAll().executeAsList()
            if(!userPrefs.userPreferencesRecord.shouldFetch && cachedRecords.isNotEmpty()) {
                return@withContext cachedRecords.map {
                    GoogleAlbum(
                        id = it.id,
                        coverPhotoUrl = it.cover_photo_url,
                        title = it.title,
                    )
                }
            }

            try {
                val response = client.get<AlbumListResponse>("$googlePhotosV1UrlString/albums") {
                    authorizeHttpRequestBuilder(this, bearerToken)
                }
                val albums = response.albums.map {
                    GoogleAlbum(it.id, "${it.coverPhotoBaseUrl}=d", it.title)
                }

                database.transaction {
                    albums.forEach {
                        database.albumsQueries.insert(
                            id = it.id,
                            cover_photo_url = it.coverPhotoUrl,
                            title = it.title,
                            downloaded = false
                        )
                    }
                }

                albums
            } catch (clientException: ClientRequestException) {
                val exceptionResponse = clientException.response
                if (exceptionResponse.status == HttpStatusCode.Unauthorized ||
                    exceptionResponse.status == HttpStatusCode.Forbidden
                ) {
                    userPrefs.clearAuthToken()
                }
                emptyList()
            } catch (ex: Throwable) {
                throw ex
            }
        }
    }

    private suspend fun getMediaItems(
        albumId: String,
        pageToken: String? = null
    ): MediaItemSearchResponseWT {
        val media = MediaItemSearchRequest(albumId, 100, pageToken)
        return withContext(Dispatchers.IO) {
            try {
                val mediaItemSearchResponseWT =
                    client.post<MediaItemSearchResponseWT>("$googlePhotosV1UrlString/mediaItems:search") {
                        body = media
                        contentType(ContentType.Application.Json)
                        headers.append("Authorization", bearerToken)
                    }
                return@withContext mediaItemSearchResponseWT
            } catch (clientException: ClientRequestException) {
                val exceptionResponse = clientException.response
                if (exceptionResponse.status == HttpStatusCode.Unauthorized ||
                    exceptionResponse.status == HttpStatusCode.Forbidden
                ) {
                    userPrefs.clearAuthToken()
                }
                throw clientException
            } catch (ex: Throwable) {
                throw ex
            }
        }
    }

    companion object {
        const val googlePhotosV1UrlString = "https://photoslibrary.googleapis.com/v1"

        private fun authorizeHttpRequestBuilder(
            httpRequestBuilder: HttpRequestBuilder,
            bearerToken: String
        ) {
            httpRequestBuilder.headers.append("Authorization", bearerToken)
            httpRequestBuilder.contentType(ContentType.Application.Json)
        }
    }
}
