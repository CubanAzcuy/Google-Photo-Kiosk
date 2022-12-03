package gives.robert.kiosk.gphotos

import coil.Coil
import gives.robert.kiosk.gphotos.utils.providers.ImageLoaderProvider
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences

class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        UserPreferences.init(context = applicationContext)

        val imageLoader = ImageLoaderProvider.newImageLoader(this)
        Coil.setImageLoader(imageLoader)
    }
}