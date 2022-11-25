package gives.robert.kiosk.gphotos.features.display

import gives.robert.kiosk.gphotos.features.display.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.display.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.display.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.display.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.utils.BasePresenter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class GooglePhotoPresenter(
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
        val asdfasdf = googleGooglePhotoRepo.fetchPhotos(setOf(testAlbum))

        val asdfffasdf = asdfasdf.associate {
            it.id to Pair("${it.baseUrl}=d", it.mimeType)
        }

        stateFlow.update {
            it.copy(photoUrls = asdfffasdf.values.toList())
        }

        job?.cancel()
        job = infiniteScope.launch {
            while (true) {
                delay(TimeUnit.SECONDS.toMillis(5))
                stateFlow.update {
                    it.copy(
                        photoUrls = asdfffasdf.values.toList(),
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
