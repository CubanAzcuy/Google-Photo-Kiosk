package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosUiState
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.extensions.toImageLoadingRequest
import gives.robert.kiosk.gphotos.utils.providers.CoilProvider
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class GooglePhotoScrollableDisplayPresenter(
    private val userPrefs: UserPreferences,
    private val googleGooglePhotoRepo: GooglePhotoRepository,
    private var coilProvider: CoilProvider,
    private var localRepo: GooglePhotoDisplayLocalRepo = GooglePhotoDisplayLocalRepo(coilProvider = coilProvider),
) : BasePresenter<DisplayPhotoEvents, DisplayPhotosUiState, DisplayPhotosEffect>() {

    override val baseState: DisplayPhotosUiState
        get() = DisplayPhotosUiState()

    private val infiniteScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    init {
        infiniteScope.launch {
            userPrefs.preferencesFlow.collect {
                if (it.authToken == null) {
                    job?.cancel()
                    job = null
                } else if (job == null && localRepo.hasCache()) {
                    onScrollDone(localRepo.getCurrentlyDisplayPhotoIndex())
                }
            }
        }
    }

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
        photoDataList.forEach {
            val request = it.toImageLoadingRequest(coilProvider.imageBuilder)
            coilProvider.imageLoader.enqueue(request)
        }

        stateFlow.update {
            it.copy(photoUrls = localRepo.getOnScreenPhotosForDisplay())
        }
    }

    private suspend fun onScrollDone(currentIndex: Int) {
        localRepo.setCurrentlyDisplayPhotoIndex(currentIndex)
        val mediaItem = localRepo.getOnScreenPhotosForProcessing()[currentIndex]
        googleGooglePhotoRepo.saveSeenPhoto(mediaItem)

        job?.cancel()
        job = infiniteScope.launch {
            delay(TimeUnit.SECONDS.toMillis(25))
            localRepo.prepareListForNextPhotoToDisplay()
            val photoToDisplay = localRepo.getOnScreenPhotosForDisplay()
            val indexToRender =  localRepo.getCurrentlyDisplayPhotoIndex()
            coilProvider.imageLoader.enqueue(photoToDisplay[indexToRender].request)

            stateFlow.update {
                it.copy(
                    photoUrls = photoToDisplay,
                    currentIndex = indexToRender
                )
            }
        }
    }
}