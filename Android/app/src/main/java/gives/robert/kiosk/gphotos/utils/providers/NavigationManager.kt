package gives.robert.kiosk.gphotos.utils.providers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class NavigationLocations {
    PHOTOS_DISPLAY,
    ALBUM_SELECT,
    DEFAULT,
    SETUP_WIFI,
    FIRST_AUTH;
}

class NavigationManager {
    val currentLocationFlow = MutableStateFlow(NavigationLocations.DEFAULT)

}
