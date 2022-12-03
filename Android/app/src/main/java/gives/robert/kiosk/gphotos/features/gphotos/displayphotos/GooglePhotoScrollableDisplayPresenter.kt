package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.ItemHolderRandom
import gives.robert.kiosk.gphotos.utils.providers.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.*
import java.util.concurrent.TimeUnit

class GooglePhotoScrollableDisplayPresenter(
    private val userPrefs: UserPreferences,
    private val googleGooglePhotoRepo: GooglePhotoRepository,
    private var localRepo: GooglePhotoDisplayLocalRepo = GooglePhotoDisplayLocalRepo()
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

class GooglePhotoDisplayLocalRepo(
    private val itemHolderRandom: ItemHolderRandom<GoogleMediaItem> = ItemHolderRandom()
) {

    private val allPhotoUrls = mutableListOf<GoogleMediaItem>()

    private var activelyDisplayedPhotos: Queue<GoogleMediaItem> = LinkedList();
    private var activelyDisplayedPhotoQueueSize = MaxWorkingListSize

    private var currentlyDisplayPhotoIndex = 0

    private val isTinyMode
        get() = allPhotoUrls.size <= MaxWorkingListSize * 1.5

    fun setPhotoList(list: List<GoogleMediaItem>) {
        allPhotoUrls.clear()
        allPhotoUrls.addAll(list)

        itemHolderRandom.setup(list)

        activelyDisplayedPhotos.clear()
        activelyDisplayedPhotoQueueSize =
            if (MaxWorkingListSize > list.size) list.size else MaxWorkingListSize
        currentlyDisplayPhotoIndex = 0


        if (isTinyMode) {
            activelyDisplayedPhotos.addAll(allPhotoUrls.shuffled())
            return
        }
        for (i in 0 until activelyDisplayedPhotoQueueSize) {
            activelyDisplayedPhotos.add(itemHolderRandom.nextRandomItem())
        }

    }

    fun prepareListForNextPhotoToDisplay() {
        currentlyDisplayPhotoIndex++

        if (isTinyMode) {
            requestNextPhotoToDisplayForSmallList()
        } else {
            requestNextPhotoToDisplayForLargerList()
        }
    }

    private fun requestNextPhotoToDisplayForLargerList() {
        if (currentlyDisplayPhotoIndex < activelyDisplayedPhotos.size) return;

        var item = itemHolderRandom.nextRandomItem()
        item = if (item == null) {
            itemHolderRandom.setup(allPhotoUrls, activelyDisplayedPhotos.toList())
            itemHolderRandom.nextRandomItem()
        } else {
            item
        }

        activelyDisplayedPhotos.add(item)
        activelyDisplayedPhotos.remove()
    }

    private fun requestNextPhotoToDisplayForSmallList() {
        if (currentlyDisplayPhotoIndex >= allPhotoUrls.size) {
            currentlyDisplayPhotoIndex = 0
        }
    }

    fun getOnScreenPhotos(): List<GoogleMediaItem> {
        if(isTinyMode) return allPhotoUrls
        return activelyDisplayedPhotos.toList()
    }

    fun getCurrentlyDisplayPhotoIndex(): Int {
        return currentlyDisplayPhotoIndex
    }

    fun setCurrentlyDisplayPhotoIndex(currentIndex: Int) {
        this.currentlyDisplayPhotoIndex = currentIndex
    }

    companion object {
        const val MaxWorkingListSize = 25
    }
}

