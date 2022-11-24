package gives.robert.kiosk.gphotos.networking

import gives.robert.kiosk.gphotos.networking.models.*
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GooglePhotoRepository(private val localIp: String) {
    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    var authToken: String? = null

    suspend fun authenticate(token: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.post<AccessTokenResponse>("http://$localIp/v1/authorize") {
                    body = TokenRequest(token)
                    contentType(ContentType.Application.Json)
                    headers {
                        append("X-Requested-With", "auth_server_token")
                    }
                }
                authToken = response.access_token
            } catch (ex: java.lang.Exception) {
                val asdfasdfasd = ex
                throw ex
            }
        }
    }

    suspend fun fetchPhotos(albumIds: Set<String>): List<MediaItems> {
        val mediaList = mutableListOf<MediaItems>()
        albumIds.forEach {
            mediaList.addAll(getMediaItems(it).mediaItems)
        }
        return mediaList
    }

    suspend fun getAlbumList() = withContext(Dispatchers.IO) {
        val bearerToken = "Bearer $authToken"

        val response = client.get<AlbumListResponse>("https://photoslibrary.googleapis.com/v1/albums") {
            contentType(ContentType.Application.Json)
            headers.append("Authorization", bearerToken)
        }
    }

    suspend fun getMediaItems(albumId: String): MediaItemSearchResponse {

        val media = MediaItemSearchRequest(albumId)
        val bearerToken = "Bearer $authToken"

        return withContext(Dispatchers.IO) {
            try {
                val asdfadsf = client.post<MediaItemSearchResponse>("https://photoslibrary.googleapis.com/v1/mediaItems:search") {
                    body = media
                    contentType(ContentType.Application.Json)
                    headers.append("Authorization", bearerToken)
                }
                return@withContext asdfadsf
            } catch (ex: Throwable) {
                throw ex
            }
        }
    }
}
