package gives.robert.kiosk.gphotos.functions.models

import java.util.InvalidPropertiesFormatException

enum class HTTPMethod {
    POST, GET, PUT, PATCH, DELETE;

    companion object {
        fun fromString(value: String): HTTPMethod {
            return when(value) {
                "POST" -> POST
                "GET" -> GET
                "PUT" -> PUT
                "PATCH" -> PATCH
                "DELETE" -> DELETE
                else -> {
                    throw InvalidPropertiesFormatException("Not Found")
                }
            }
        }
    }
}

val String.asHTTPMethod: HTTPMethod
    get() = HTTPMethod.fromString(this)

