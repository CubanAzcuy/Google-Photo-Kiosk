package gives.robert.kiosk.gphotos.utils.providers

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import gives.robert.kiosk.gphotos.utils.extensions.defaultImageLoader

class CoilProvider(
    val imageBuilder: ImageRequest.Builder,
    val imageLoader: ImageLoader
) {
    companion object {
        @Volatile
        private lateinit var instance: CoilProvider

        fun getInstance(context: Context): CoilProvider {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = CoilProvider(
                        ImageRequest.Builder(context),
                        context.defaultImageLoader()
                    )
                }
                return instance
            }
        }
    }

}