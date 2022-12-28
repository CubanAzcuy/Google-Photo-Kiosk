package gives.robert.kiosk.gphotos.utils.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.request.ImageRequest
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleAlbum
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import gives.robert.kiosk.gphotos.utils.FileBasedCacheInterceptor
import gives.robert.kiosk.gphotos.utils.providers.HttpClientProvider.client
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import java.io.File

val String.getFileNameFromDirectory: String
    get() {
        val splitPath = split("/")
        return splitPath[splitPath.size - 1]
    }

suspend fun HttpClient.downloadFile(path: String, rootDir: String) {
    val fileName = path.getFileNameFromDirectory
    val file = File("$rootDir/$fileName")
    downloadFile(path, file)
}

suspend fun HttpClient.downloadFile(path: String, file: File) {
    runBlocking {
        val httpResponse = client.get<HttpResponse>(path)
        val responseBody = httpResponse.readBytes()
        file.writeBytes(responseBody)
    }
}

var imageLoader: ImageLoader? = null
fun Context.defaultImageLoader(): ImageLoader {
    if(imageLoader != null) {
        return imageLoader!!
    }

    val lruCache = LruCache<String, Drawable>(25)
    val imageLoader2 = ImageLoader.Builder(this)
        .components {
            add(
                FileBasedCacheInterceptor(
                    this@defaultImageLoader,
                    client,
                    lruCache,
                )
            )
        }
        .build()
    imageLoader = imageLoader2
    return imageLoader2
}

fun GoogleAlbum.toImageLoadingRequest(builder: ImageRequest.Builder): ImageRequest {
    return builder
        .data(coverPhotoUrl)
        .diskCacheKey("$title.webp")
        .build()
}

fun GoogleMediaItem.toImageLoadingRequest(builder: ImageRequest.Builder): ImageRequest {
    return builder
        .data(baseUrl)
        .diskCacheKey(fileName)
        .build()
}
