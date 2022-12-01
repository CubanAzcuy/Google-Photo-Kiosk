package gives.robert.kiosk.gphotos.utils.providers

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

object ImageLoaderProvider {

    private const val BYTE_STEP_UP = 1024L

    fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.4)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(20 * BYTE_STEP_UP * BYTE_STEP_UP * BYTE_STEP_UP)
                    .build()
            }
            .build()
    }
}