package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosUiState
import gives.robert.kiosk.gphotos.utils.BasePresenter
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
    private var lastRequestedKey: String? = null
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
                println("-----------------------------------------")
                println("-------------Cancel-Timer----------------")
                println("-----------------------------------------")

                job?.cancel()
            }
        }
    }

    private suspend fun buildPhotoList() {
        val photoDataList = googleGooglePhotoRepo.fetchPhotos()
        localRepo.setPhotoList(photoDataList)
        val onscreenPhotos = localRepo.getOnScreenPhotosForDisplay()

        onscreenPhotos.forEach {
            coilProvider.imageLoader.enqueue(it.request)
        }

        stateFlow.update {
            it.copy(photoUrls = localRepo.getOnScreenPhotosForDisplay())
        }
    }

    private suspend fun onScrollDone(currentlyDisplayedIndex: Int) {

        val requestedKey = localRepo.getOnScreenPhotosForDisplay()[currentlyDisplayedIndex].id

        if (lastRequestedKey == requestedKey && job?.isCancelled == false) {
            return
        }

        lastRequestedKey = requestedKey
        localRepo.setCurrentlyDisplayPhotoIndex(currentlyDisplayedIndex)

        job?.cancel()
        job = infiniteScope.launch {
            println("-----------------------------------------")
            println("--------------Start-Timer----------------")
            println("-----------------------------------------")
            delay(TimeUnit.SECONDS.toMillis(25))
            println("-----------------------------------------")
            println("---------------End-Timer-----------------")
            println("-----------------------------------------")

            localRepo.prepareListForNextPhotoToDisplay()
            val photoToDisplay = localRepo.getOnScreenPhotosForDisplay()
            val indexToRender = localRepo.getCurrentlyDisplayPhotoIndex()

            googleGooglePhotoRepo.saveSeenPhoto(localRepo.getOnScreenPhotosForProcessing()[indexToRender])
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