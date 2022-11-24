package gives.robert.kiosk.gphotos.functions

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.cloud.functions.HttpRequest
import java.nio.file.InvalidPathException
import java.util.*

object V1Functions {
    private val gsonFactory = GsonFactory()

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
        if(request.getFirstHeader("X-Requested-With").orElse("") != null) {
            throw InvalidPathException(request.path, "Unsupported Path")
        }

        val code = request.getFirstHeader("Authorization")
                .orElse("Bearer ")
                .split("Bearer ")
                .filter { it.isNotBlank() }[0]

        val tokenResponse = GoogleAuthorizationCodeTokenRequest(
                NetHttpTransport(),
                gsonFactory,
                "https://oauth2.googleapis.com/token",
                "",
                "",
                code,
                "") // Specify the same redirect URI that you use with your web
                // app. If you don't have a web version of your app, you can
                // specify an empty string.
                .execute()

        val json = mapOf("token" to tokenResponse.accessToken)
        return StubbedResponse(body = gsonFactory.toPrettyString(json))
    }

}