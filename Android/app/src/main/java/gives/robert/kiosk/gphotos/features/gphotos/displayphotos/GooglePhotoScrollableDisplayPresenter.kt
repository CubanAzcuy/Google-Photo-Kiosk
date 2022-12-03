package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosUiState
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class GooglePhotoScrollableDisplayPresenter(
    private val userPrefs: UserPreferences,
    private val googleGooglePhotoRepo: GooglePhotoRepository,
    private var localRepo: GooglePhotoDisplayLocalRepo = GooglePhotoDisplayLocalRepo()
) : BasePresenter<DisplayPhotoEvents, DisplayPhotosUiState, DisplayPhotosEffect>() {

    override val baseState: DisplayPhotosUiState
        get() = DisplayPhotosUiState()

    private val infiniteScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    override suspend fun handleEvent(event: DisplayPhotoEvents) {
        when (event) {
            DisplayPhotoEvents.GetPhotos -> {
                buildPhotoList()
            }
            is DisplayPhotoEvents.ScrollingStopped -> {
                onScrollDone(event.currentIndex)
            }
            DisplayPhotoEvents.OnAuthLost -> {
                userPrefs.clearAuthToken()
            }
            else -> {
                job?.cancel()
            }
        }
    }

    private suspend fun buildPhotoList() {
        val photoDataList = googleGooglePhotoRepo.fetchPhotos()
        localRepo.setPhotoList(photoDataList)

        stateFlow.update {
            it.copy(photoUrls = localRepo.getOnScreenPhotos())
        }
    }

    private suspend fun onScrollDone(currentIndex: Int) {
        localRepo.setCurrentlyDisplayPhotoIndex(currentIndex)
        val mediaItem = localRepo.getOnScreenPhotos()[currentIndex]
        googleGooglePhotoRepo.saveSeenPhoto(mediaItem)

        job?.cancel()
        job = infiniteScope.launch {
            delay(TimeUnit.SECONDS.toMillis(5))
            localRepo.prepareListForNextPhotoToDisplay()
            stateFlow.update {
                it.copy(
                    photoUrls = localRepo.getOnScreenPhotos(),
                    currentIndex = localRepo.getCurrentlyDisplayPhotoIndex()
                )
            }
        }
    }
}