package gives.robert.kiosk.gphotos.features.gphotos.data

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
    private val userPrefs: UserPreferences
) {

    private val bearerToken
        get() = "Bearer ${userPrefs.userPreferencesRecord.authToken}"

    suspend fun fetchPhotos(): List<GoogleMediaItem> {
        return fetchPhotos(userPrefs.userPreferencesRecord.selectedAlbumIds)
    }

    private suspend fun fetchPhotos(albumIds: Set<String>): List<GoogleMediaItem> {
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
        return mediaList
    }

    private fun googleMediaItemsWTtoGoogleMediaItems(
        mediaItemResponse: MediaItemSearchResponseWT,
        albumId: String
    ) = mediaItemResponse.mediaItems.filter {
        it.baseUrl != null
    }.map { googleMediaItem ->
        GoogleMediaItem(
            googleMediaItem.id,
            "${googleMediaItem.baseUrl}=d",
            googleMediaItem.mimeType,
            albumId
        )
    }

    suspend fun fetchAlbums(): List<GoogleAlbum> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get<AlbumListResponse>("$googlePhotosV1UrlString/albums") {
                    authorizeHttpRequestBuilder(this, bearerToken)
                }
                response.albums.map {
                    GoogleAlbum(it.id, "${it.coverPhotoBaseUrl}=d", it.title)
                }
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
