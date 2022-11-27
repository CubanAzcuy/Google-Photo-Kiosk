package gives.robert.kiosk.gphotos.features.gphotos.networking

import gives.robert.kiosk.gphotos.features.gphotos.networking.models.*
import gives.robert.kiosk.gphotos.utils.UserPreferences
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GooglePhotoRepository(
    private val client: HttpClient,
    private val userPrefs: UserPreferences
) {

    private val bearerToken
        get() = "Bearer ${userPrefs.userPreferencesRecord.authToken}"

    suspend fun fetchPhotos(): List<MediaItems> {
        return fetchPhotos(userPrefs.userPreferencesRecord.selectedAlbumIds)
    }

    private suspend fun fetchPhotos(albumIds: Set<String>): List<MediaItems> {
        val mediaList = mutableListOf<MediaItems>()
        albumIds.forEach {
            try {
                mediaList.addAll(getMediaItems(it).mediaItems)
            } catch (ex: Exception){
                //no-op
            }
        }
        return mediaList
    }

    suspend fun getAlbums(): List<Albums> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get<AlbumListResponse>("$googlePhotosV1UrlString/albums") {
                    authorizeHttpRequestBuilder(this, bearerToken)
                }
                response.albums
            } catch (clientException: ClientRequestException) {
                val exceptionResponse = clientException.response
                if (exceptionResponse.status == HttpStatusCode.Unauthorized ||
                    exceptionResponse.status == HttpStatusCode.Forbidden) {
                    userPrefs.clearAuthToken()
                }
                emptyList()
            } catch (ex: Throwable) {
                throw ex
            }
        }
    }

    private suspend fun getMediaItems(albumId: String): MediaItemSearchResponse {
        val media = MediaItemSearchRequest(albumId)
        return withContext(Dispatchers.IO) {
            try {
                val mediaItemSearchResponse =
                    client.post<MediaItemSearchResponse>("$googlePhotosV1UrlString/mediaItems:search") {
                        body = media
                        contentType(ContentType.Application.Json)
                        headers.append("Authorization", bearerToken)
                    }
                return@withContext mediaItemSearchResponse
            } catch (clientException: ClientRequestException) {
                val exceptionResponse = clientException.response
                if (exceptionResponse.status == HttpStatusCode.Unauthorized ||
                    exceptionResponse.status == HttpStatusCode.Forbidden) {
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
