@file:Suppress("unused")

package gives.robert.kiosk.gphotos.functions

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import mu.KotlinLogging
import java.io.IOException
import java.nio.file.InvalidPathException

class App : HttpFunction {

    private val logger = KotlinLogging.logger {}

    @Throws(IOException::class)
    override fun service(request: HttpRequest, response: HttpResponse) {
//        response.writer.write(request.getFirstHeader("Authorization").orElse("Bearer "))

        val afasdfasdf = runVersion(request)
        logger.info { "hello world" }
        response.writer.write(afasdfasdf.body)

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
