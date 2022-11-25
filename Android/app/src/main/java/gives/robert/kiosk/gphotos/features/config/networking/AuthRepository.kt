package gives.robert.kiosk.gphotos.features.config.networking

import gives.robert.kiosk.gphotos.features.config.networking.models.AccessTokenResponse
import gives.robert.kiosk.gphotos.features.config.networking.models.TokenRequest
import gives.robert.kiosk.gphotos.utils.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.UserPreferences
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class AuthRepository(
    private val localIp: String,
    private val userPrefs: UserPreferences,
    private val client: HttpClient,
) {
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
                userPrefs.saveAuthToken(response.access_token)
            } catch (ex: Exception) {
                val asdfasdfasd = ex
                throw ex
            }
        }
    }
}
