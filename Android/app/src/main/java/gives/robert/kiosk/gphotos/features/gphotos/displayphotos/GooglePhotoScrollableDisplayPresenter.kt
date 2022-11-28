package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.BetterRandom
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class GooglePhotoDisplayLocalRepo {
    private val allPhotoUrls = mutableListOf<Pair<String, String>>()
    private val workingPhotoUrls = mutableListOf<Pair<String, String>>()

    private var workingMaxIndex = MaxWorkingListSize
    private var headIndex = 0
    private var tailIndex = workingMaxIndex
    private var currentIndex = 0

    fun setPhotoList(list: List<Pair<String, String>>) {
        allPhotoUrls.clear()
        allPhotoUrls.addAll(list)
        workingPhotoUrls.clear()

        headIndex = 0
        workingMaxIndex = if (MaxWorkingListSize > list.size) list.size else MaxWorkingListSize
        tailIndex = workingMaxIndex
        currentIndex = 0

        for(i in headIndex until tailIndex) {
            val index = BetterRandom.getUnusedNext(0 until allPhotoUrls.size)
            workingPhotoUrls.add(allPhotoUrls[index])
        }

    }

    fun getWorkingList(): List<Pair<String, String>> {
        return workingPhotoUrls.slice(headIndex until tailIndex)
    }

    fun increaseIndex() {
        currentIndex++

        if (currentIndex >= workingMaxIndex) {
            headIndex++
            tailIndex++
            val index = BetterRandom.getUnusedNext(0 until allPhotoUrls.size)
            workingPhotoUrls.add(allPhotoUrls[index])
        }

        if (tailIndex >= allPhotoUrls.size) {
            headIndex = 0
            tailIndex = workingMaxIndex
            currentIndex = 0
        }

        //Un-need peace of mind check
        if (currentIndex > tailIndex) {
            currentIndex = tailIndex
        }
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

        val photoMap = photoDataList.associate {
            it.id to Pair("${it.baseUrl}=d", it.mimeType)
        }
        val photoUrlList = photoMap.values.toList()

        localRepo.setPhotoList(photoUrlList)

        stateFlow.update {
            it.copy(photoUrls = localRepo.getWorkingList())
        }
    }

    private suspend fun onScrollDone(currentIndex: Int) {
        localRepo.setWorkingIndex(currentIndex)
//
        job?.cancel()
        job = infiniteScope.launch {
            delay(TimeUnit.SECONDS.toMillis(5))
            localRepo.increaseIndex()
            stateFlow.update {
                it.copy(
                    photoUrls = localRepo.getWorkingList(),
                    currentIndex = localRepo.getWorkingIndex()
                )
            }
        }
    }
}
