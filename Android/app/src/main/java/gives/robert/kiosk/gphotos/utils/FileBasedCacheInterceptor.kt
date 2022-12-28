package gives.robert.kiosk.gphotos.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.SuccessResult
import gives.robert.kiosk.gphotos.features.gphotos.data.OnlineGooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.models.wt.MediaItemSearchResponseWT
import gives.robert.kiosk.gphotos.features.gphotos.data.models.wt.MediaItems
import gives.robert.kiosk.gphotos.utils.extensions.downloadFile
import gives.robert.kiosk.gphotos.utils.extensions.getFileNameFromDirectory
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileBasedCacheInterceptor(
    private val context: Context,
    private val client: HttpClient,
    private val userPrefs: UserPreferences,
    private val cache: LruCache<String, Drawable>
) : Interceptor {

    private val bearerToken
        get() = "Bearer ${userPrefs.userPreferencesRecord.authToken}"

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {

        return withContext(Dispatchers.IO) {
            val key = chain.request.diskCacheKey ?: chain.request.data.toString()
            val path = chain.request.data.toString()

            if (cache.get(key) != null) {
                withContext(Dispatchers.Main) {
                    return@withContext SuccessResult(
                        drawable = cache.get(key)!!,
                        request = chain.request,
                        dataSource = DataSource.MEMORY_CACHE
                    )
                }
            }

            val fileName = key.getFileNameFromDirectory

            val file = File("${context.applicationInfo.dataDir}/${fileName}")

            if (file.exists()) {
                return@withContext saveDrawableAndReturnResult(file, key, chain)
            }

            try {
                val url = client.get<MediaItems>("${OnlineGooglePhotoRepository.googlePhotosV1UrlString}/mediaItems/${chain.request.memoryCacheKey!!.key}") {
                    headers.append("Authorization", bearerToken)
                }.baseUrl
                client.downloadFile(url!!, file)
            } catch (ex: Throwable) {
                val adsfsadf = (ex as ClientRequestException).response
                return@withContext ErrorResult(
                    null,
                    request = chain.request,
                    throwable = ex
                )
            }
            if (file.exists()) {
                return@withContext saveDrawableAndReturnResult(file, key, chain)
            }

            withContext(Dispatchers.Main) {
                return@withContext chain.proceed(chain.request)
            }
        }
    }

    private suspend fun saveDrawableAndReturnResult(
        file: File,
        key: String,
        chain: Interceptor.Chain
    ): SuccessResult {
        val drawable = Drawable.createFromPath(file.path)!!
        cache.put(key, drawable)

        return withContext(Dispatchers.Main) {
            return@withContext SuccessResult(
                drawable = drawable,
                request = chain.request,
                dataSource = DataSource.DISK
            )
        }
    }
}
