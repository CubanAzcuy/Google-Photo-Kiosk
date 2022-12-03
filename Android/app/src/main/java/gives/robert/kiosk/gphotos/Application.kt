package gives.robert.kiosk.gphotos

import gives.robert.kiosk.gphotos.utils.providers.UserPreferences

class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        UserPreferences.init(context = applicationContext)

    }
}