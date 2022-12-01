package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.gphotos.data.GooglePhotoRepository
import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.GetNextGoogleMediaItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.*
import java.util.concurrent.TimeUnit

class GooglePhotoScrollableDisplayPresenter(
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
            else -> {
                job?.cancel()
            }
        }
    }

    private suspend fun buildPhotoList() {
        val photoDataList = googleGooglePhotoRepo.fetchPhotos()
        localRepo.setPhotoList(photoDataList)

        stateFlow.update {
            it.copy(photoUrls = localRepo.getWorkingList())
        }
    }

    private suspend fun onScrollDone(currentIndex: Int) {
        localRepo.setWorkingIndex(currentIndex)
        val mediaItem = localRepo.getWorkingList()[currentIndex]
        googleGooglePhotoRepo.saveSeenPhoto(mediaItem)

        job?.cancel()
        job = infiniteScope.launch {
            delay(TimeUnit.SECONDS.toMillis(5))
            localRepo.step()
            stateFlow.update {
                it.copy(
                    photoUrls = localRepo.getWorkingList(),
                    currentIndex = localRepo.getWorkingIndex()
                )
            }
        }
    }
}

class GooglePhotoDisplayLocalRepo {
    private val allPhotoUrls = mutableListOf<GoogleMediaItem>()
    private var workingPhotoUrls: Queue<GoogleMediaItem> = LinkedList();

    private var workingMaxIndex = MaxWorkingListSize
    private var currentIndex = 0

    private val isTinyMode = allPhotoUrls.size >= MaxWorkingListSize * 1.5

    fun setPhotoList(list: List<GoogleMediaItem>) {
        allPhotoUrls.clear()
        allPhotoUrls.addAll(list)
        workingPhotoUrls.clear()
        workingMaxIndex = if (MaxWorkingListSize > list.size) list.size else MaxWorkingListSize
        currentIndex = 0

        for (i in 0 until workingMaxIndex) {
            val index = GetNextGoogleMediaItem.getUnusedNext(allPhotoUrls)
            workingPhotoUrls.add(allPhotoUrls[index])
        }
    }

    fun step() {
        currentIndex++

        if (isTinyMode) {
            currentIndex++
            if (currentIndex >= allPhotoUrls.size) {
                currentIndex = 0
            }
            return
        }

        if (currentIndex == workingMaxIndex) {
            currentIndex = workingMaxIndex

            val newIndex = try {
                GetNextGoogleMediaItem.getUnusedNext(allPhotoUrls)
            } catch (ex: ArrayIndexOutOfBoundsException) {
                GetNextGoogleMediaItem.reset(workingPhotoUrls)
                GetNextGoogleMediaItem.getUnusedNext(allPhotoUrls)
            }
            workingPhotoUrls.add(allPhotoUrls[newIndex])
            workingPhotoUrls.remove()
        }
    }
    
    fun getWorkingList(): List<GoogleMediaItem> {
        if(isTinyMode) return allPhotoUrls
        return workingPhotoUrls.toList()
    }

    fun getWorkingIndex(): Int {
        return currentIndex
    }

    fun setWorkingIndex(currentIndex: Int) {
        this.currentIndex = currentIndex
    }

    companion object {
        const val MaxWorkingListSize = 25
    }
}

