package gives.robert.kiosk.gphotos

import android.util.Log
import coil.Coil
import gives.robert.kiosk.gphotos.utils.extensions.defaultImageLoader
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences

class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        UserPreferences.init(context = applicationContext)

        Log.d("APP", "LAUNCHED")
        val imageLoader = defaultImageLoader()
        Coil.setImageLoader(imageLoader)
    }
}