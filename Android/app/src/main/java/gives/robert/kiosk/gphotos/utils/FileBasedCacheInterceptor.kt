package gives.robert.kiosk.gphotos.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.core.util.lruCache
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import gives.robert.kiosk.gphotos.utils.extensions.downloadFile
import gives.robert.kiosk.gphotos.utils.extensions.getFileNameFromDirectory
import io.ktor.client.*
import java.io.File

class FileBasedCacheInterceptor(
    private val context: Context,
    private val client: HttpClient,
    private val cache: LruCache<String, Drawable>
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {

        val key = chain.request.diskCacheKey ?: chain.request.data.toString()
        val path = chain.request.data.toString()

        if (cache.get(key) != null) {
            return SuccessResult(
                drawable = cache.get(key)!!,
                request = chain.request,
                dataSource = DataSource.MEMORY_CACHE
            )
        }

        val fileName = key.getFileNameFromDirectory

        val file = File("${context.applicationInfo.dataDir}/${fileName}")

        if (file.exists()) {
            return saveDrawableAndReturnResult(file, key, chain)
        }

        client.downloadFile(path, file)
        if (file.exists()) {
            return saveDrawableAndReturnResult(file, key, chain)
        }

        return chain.proceed(chain.request)
    }

    private fun saveDrawableAndReturnResult(
        file: File,
        key: String,
        chain: Interceptor.Chain
    ): SuccessResult {
        val drawable = Drawable.createFromPath(file.path)!!
        cache.put(key, drawable)

        return SuccessResult(
            drawable = drawable,
            request = chain.request,
            dataSource = DataSource.DISK
        )
    }
}
