package gives.robert.kiosk.gphotos.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class NavigationLocations {
    WIFI_SELECT,
    PHOTOS_DISPLAY,
    ALBUM_SELECT;
}

class NavigationManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentLocationFlow = MutableStateFlow(NavigationLocations.WIFI_SELECT)
    val currentLocationFlow: StateFlow<NavigationLocations> = _currentLocationFlow

    fun gotoLocation(locations: NavigationLocations) {
        scope.launch {
            _currentLocationFlow.emit(locations)
        }
    }
}
