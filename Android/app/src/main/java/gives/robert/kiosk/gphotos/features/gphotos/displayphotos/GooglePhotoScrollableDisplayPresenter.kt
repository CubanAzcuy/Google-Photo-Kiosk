package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.utils.BasePresenter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class GooglePhotoScrollableDisplayPresenter(
    private val googleGooglePhotoRepo: GooglePhotoRepository,
    private val testAlbum: String
) : BasePresenter<DisplayPhotoEvents, DisplayPhotosState, DisplayPhotosEffect>() {

    override val baseState: DisplayPhotosState
        get() = DisplayPhotosState()

    private val infiniteScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    override suspend fun handleEvent(event: DisplayPhotoEvents) {
        when (event) {
            DisplayPhotoEvents.GetPhotos -> {
                buildPhotoList()
            }
        }
    }

    private suspend fun buildPhotoList() {
        val photoDataList = googleGooglePhotoRepo.fetchPhotos()

        val photoMap = photoDataList.associate {
            it.id to Pair("${it.baseUrl}=d", it.mimeType)
        }
        val photoUrlList = photoMap.values.toList()

        stateFlow.update {
            it.copy(photoUrls = photoUrlList)
        }

        job?.cancel()
        job = infiniteScope.launch {
            while (true) {
                delay(TimeUnit.SECONDS.toMillis(5))
                stateFlow.update {
                    it.copy(
                        photoUrls = photoUrlList,
                        currentIndex = it.currentIndex + 1
                    )
                }
            }
        }
    }

    fun onClear() {
        job?.cancel()
        job = null
    }
}
