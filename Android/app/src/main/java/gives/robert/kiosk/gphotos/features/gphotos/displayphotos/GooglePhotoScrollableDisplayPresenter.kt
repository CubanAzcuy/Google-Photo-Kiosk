package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotoEvents
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosEffect
import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data.DisplayPhotosState
import gives.robert.kiosk.gphotos.features.gphotos.networking.GooglePhotoRepository
import gives.robert.kiosk.gphotos.utils.BasePresenter
import gives.robert.kiosk.gphotos.utils.BetterRandom
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.util.*
import java.util.concurrent.TimeUnit

typealias ThingsINeed = Pair<String, String>

class GooglePhotoDisplayLocalRepo {
    private val allPhotoUrls = mutableListOf<ThingsINeed>()
    private var workingPhotoUrls: Queue<ThingsINeed> = LinkedList();

    private var workingMaxIndex = MaxWorkingListSize
    private var currentIndex = 0

    private val isTinyMode = allPhotoUrls.size >= MaxWorkingListSize * 1.5

    fun setPhotoList(list: List<ThingsINeed>) {
        allPhotoUrls.clear()
        allPhotoUrls.addAll(list)
        workingPhotoUrls.clear()
        workingMaxIndex = if (MaxWorkingListSize > list.size) list.size else MaxWorkingListSize
        currentIndex = 0

        for (i in 0 until workingMaxIndex) {
            val index = BetterRandom.getUnusedNext(allPhotoUrls)
            workingPhotoUrls.add(allPhotoUrls[index])
        }
    }

    fun getWorkingList(): List<ThingsINeed> {
        currentIndex++

        if (isTinyMode) {
            currentIndex++
            if (currentIndex >= allPhotoUrls.size) {
                currentIndex = 0
            }
            return allPhotoUrls
        }

        if (currentIndex == workingMaxIndex) {
            currentIndex = workingMaxIndex

            val newIndex = try {
                BetterRandom.getUnusedNext(allPhotoUrls)
            } catch (ex: ArrayIndexOutOfBoundsException) {
                BetterRandom.reset(workingPhotoUrls)
                BetterRandom.getUnusedNext(allPhotoUrls)
            }
            workingPhotoUrls.add(allPhotoUrls[newIndex])
            workingPhotoUrls.remove()
        }

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

        job?.cancel()
        job = infiniteScope.launch {
            delay(TimeUnit.SECONDS.toMillis(5))
            stateFlow.update {
                it.copy(
                    photoUrls = localRepo.getWorkingList(),
                    currentIndex = localRepo.getWorkingIndex()
                )
            }
        }
    }
}
