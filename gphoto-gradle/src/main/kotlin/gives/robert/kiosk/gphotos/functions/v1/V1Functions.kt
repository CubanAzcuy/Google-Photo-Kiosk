package gives.robert.kiosk.gphotos.functions.v1

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.cloud.functions.HttpRequest
import gives.robert.kiosk.gphotos.functions.models.HTTPMethod
import gives.robert.kiosk.gphotos.functions.models.StubbedResponse
import gives.robert.kiosk.gphotos.functions.models.TokenRequest
import gives.robert.kiosk.gphotos.functions.models.asHTTPMethod
import gives.robert.kiosk.gphotos.functions.utils.GsonProvider
import java.nio.file.InvalidPathException

object V1Functions {

    private val gsonFactory
        get() = GsonProvider.gsonFactory
    private val gson
        get() = GsonProvider.gson

    fun executeRequest(request: HttpRequest): StubbedResponse {
        val subPath = request.path.split("/").filter { it.isNotBlank() }
        return with(subPath[1]) {
            when {
                contains("ping") -> {
                    runPing(request)
                }

                contains("authorize") -> {
                    authorize(request)
                }

                else -> {
                    throw InvalidPathException(request.path, "Unsupported Path")
                }
            }
        }
    }

    private fun runPing(request: HttpRequest): StubbedResponse {
        TODO("Not yet implemented")
    }

    private fun authorize(request: HttpRequest): StubbedResponse {
        return when (request.method.asHTTPMethod) {
            HTTPMethod.POST -> {
                authorizePost(request)
            }
            else -> {
                throw InvalidPathException(request.path, "Unsupported Path")
            }
        }
    }

    private fun authorizePost(request: HttpRequest): StubbedResponse {
        val tokenRequest = gson.fromJson(request.reader, TokenRequest::class.java)

        if (request.getFirstHeader("X-Requested-With").orElse("") != "auth_server_token") {
            throw InvalidPathException(request.path, "Unsupported Path")
        }

        val tokenResponse = GoogleAuthorizationCodeTokenRequest(
                NetHttpTransport(),
                gsonFactory,
                "https://oauth2.googleapis.com/token",
                "",
                "",
                tokenRequest.token,
                "") // Specify the same redirect URI that you use with your web
                // app. If you don't have a web version of your app, you can
                // specify an empty string.
                .execute()

        val json = mapOf("access_token" to tokenResponse.accessToken)
        return StubbedResponse(body = gsonFactory.toPrettyString(json))
    }
}
