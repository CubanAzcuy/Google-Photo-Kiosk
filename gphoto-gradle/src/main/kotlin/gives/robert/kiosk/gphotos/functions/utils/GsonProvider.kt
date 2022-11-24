package gives.robert.kiosk.gphotos.functions.utils

import com.google.api.client.json.gson.GsonFactory
import com.google.gson.Gson

object GsonProvider {
    val gson = Gson()
    val gsonFactory = GsonFactory()
}