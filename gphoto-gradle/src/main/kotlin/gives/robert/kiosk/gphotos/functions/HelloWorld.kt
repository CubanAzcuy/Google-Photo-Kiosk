@file:Suppress("unused")

package gives.robert.kiosk.gphotos.functions

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import gives.robert.kiosk.gphotos.functions.models.StubbedResponse
import gives.robert.kiosk.gphotos.functions.v1.V1Functions
import mu.KotlinLogging
import java.io.IOException
import java.nio.file.InvalidPathException

class App : HttpFunction {

    private val logger = KotlinLogging.logger {}

    @Throws(IOException::class)
    override fun service(request: HttpRequest, response: HttpResponse) {
        val stubbedResponse = runVersion(request)
        logger.info { "hello world" }
        response.appendHeader("Content-Type","application/json")
        response.writer.write(stubbedResponse.body)
    }

    private fun runVersion(request: HttpRequest): StubbedResponse {
        return with(request.path) {
            when {
                contains("v1") -> V1Functions.executeRequest(request)
                else -> throw InvalidPathException(this, "Unsupported Path")
            }
        }
    }
}
