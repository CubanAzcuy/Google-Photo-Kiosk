package gives.robert.kiosk.gphotos.features.config.networking

import gives.robert.kiosk.gphotos.features.config.networking.models.AccessTokenResponse
import gives.robert.kiosk.gphotos.features.config.networking.models.TokenRequest
import gives.robert.kiosk.gphotos.utils.HttpClientProvider
import gives.robert.kiosk.gphotos.utils.UserPreferences
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.lang.Exception

class AuthRepository(
    private val localIp: String,
    private val userPrefs: UserPreferences,
    private val client: HttpClient,
) {
    suspend fun authenticate(token: String, retryCount: Int = 0) {
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
                when {
                    ex is java.net.ConnectException && retryCount < 20 -> {
                        delay(500L * retryCount * (retryCount/2))
                        authenticate(token, retryCount + 1)
                    }
                    else -> {
                        throw ex
                    }
                }
            }
        }
    }
}
