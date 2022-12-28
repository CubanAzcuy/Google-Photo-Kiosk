package gives.robert.kiosk.gphotos.features

import android.content.Context
import androidx.compose.runtime.*
import gives.robert.kiosk.gphotos.Albums
import gives.robert.kiosk.gphotos.Photos
import gives.robert.kiosk.gphotos.utils.extensions.ConnectionState
import gives.robert.kiosk.gphotos.utils.providers.DatabaseQueryProvider
import gives.robert.kiosk.gphotos.utils.providers.NavigationLocations
import gives.robert.kiosk.gphotos.utils.providers.NavigationManager
import gives.robert.kiosk.gphotos.utils.providers.UserPreferencesRecord

@Composable
fun SetupNavigation(
    context: Context,
    connectionState: ConnectionState,
    userPrefs: State<UserPreferencesRecord>,
    requestedLocation: NavigationManager,
): State<NavigationLocations> {
    val photoList = remember {
        DatabaseQueryProvider.getInstance(context).photoList()
    }
    val photoListState = photoList.collectAsState(initial = emptyList())

    val albumList = remember {
        DatabaseQueryProvider.getInstance(context).albumList()
    }
    val albumListState = albumList.collectAsState(initial = emptyList())

    return NavigationLocation(
        connectionState,
        albumListState,
        photoListState,
        userPrefs,
        requestedLocation,
    )
}

@Composable
fun NavigationLocation(
    connectionState: ConnectionState,
    albumListState: State<List<Albums>>,
    photoListState: State<List<Photos>>,
    userPrefsState: State<UserPreferencesRecord>,
    requestedLocations: NavigationManager
): State<NavigationLocations> {

    val navigationState = remember { mutableStateOf(NavigationLocations.DEFAULT) }

    val hasInternetConnection = connectionState == ConnectionState.Available
    val isAuthenticated = userPrefsState.value.authToken != null
    val hasPhotoCache = photoListState.value.isNotEmpty()
    val hasAlbumCache = albumListState.value.isNotEmpty()

    //Check Cache State First
    val setupWifi =  !isAuthenticated && !hasPhotoCache && !hasInternetConnection
    val mustAuthenticate = !isAuthenticated && !hasPhotoCache && hasInternetConnection
    val mustSelectPhotos = isAuthenticated && !hasAlbumCache && hasInternetConnection

    val updateLocation = requestedLocations.currentLocationFlow.collectAsState().value

    println("------------------------------------------")
    println("---------hasInternetConnection: ${hasInternetConnection}----------")
    println("---------isAuthenticated: ${isAuthenticated}----------")
    println("---------hasPhotoCache: ${hasPhotoCache}----------")
    println("---------hasAlbumCache: ${hasAlbumCache}----------")
    println("---------setupWifi: ${setupWifi}----------")
    println("---------mustAuthenticate: ${mustAuthenticate}----------")
    println("---------mustSelectPhotos: ${mustSelectPhotos}----------")
    println("---------updateLocation: ${updateLocation}----------")
    println("------------------------------------------")

    LaunchedEffect(setupWifi, mustAuthenticate, mustSelectPhotos, updateLocation) {
        navigationState.value = when {
            setupWifi -> {
                NavigationLocations.SETUP_WIFI
            }
            mustAuthenticate -> {
                NavigationLocations.FIRST_AUTH
            }
            mustSelectPhotos -> {
                NavigationLocations.ALBUM_SELECT
            }
            updateLocation != NavigationLocations.DEFAULT -> {
                updateLocation
            }
            else -> {
                navigationState.value
            }
        }
    }

    return navigationState
}