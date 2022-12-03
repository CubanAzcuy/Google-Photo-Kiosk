package gives.robert.kiosk.gphotos.features.gphotos.displayphotos

import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import gives.robert.kiosk.gphotos.utils.ItemHolderRandom
import java.util.*

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
