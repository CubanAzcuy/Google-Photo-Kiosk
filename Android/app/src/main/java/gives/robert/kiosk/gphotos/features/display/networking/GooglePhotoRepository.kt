package gives.robert.kiosk.gphotos.features.display.networking

import gives.robert.kiosk.gphotos.features.display.networking.models.*
import gives.robert.kiosk.gphotos.utils.UserPreferences
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GooglePhotoRepository(
    private val client: HttpClient,
    private val userPrefs: UserPreferences
) {

    private val bearerToken
        get() = "Bearer ${userPrefs.authToken}"

    suspend fun fetchPhotos(albumIds: Set<String>): List<MediaItems> {
        val mediaList = mutableListOf<MediaItems>()
        albumIds.forEach {
            mediaList.addAll(getMediaItems(it).mediaItems)
        }
        return mediaList
    }

    private suspend fun getAlbumList() {
        withContext(Dispatchers.IO) {
            try {
                client.get<AlbumListResponse>("$googlePhotosV1UrlString/albums") {
                    authorizeHttpRequestBuilder(this, bearerToken)
                }
            } catch (ex: Exception) {
                throw ex
                //TODO: Remove Auth Token on Error
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
            } catch (ex: Throwable) {
                throw ex
                //TODO: Remove Auth Token on Error

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
