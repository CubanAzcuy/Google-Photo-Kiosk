package gives.robert.kiosk.gphotos.utils

import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import java.util.*

interface IdItem {
    val id: Any
}

class ItemHolderRandom<T : IdItem> {

    private val workingItems = mutableListOf<T>()

    fun setup(allItems: List<T>, onScreenItems: List<T> = emptyList()) {

        workingItems.clear()

        if (onScreenItems.isEmpty()) {
            workingItems.addAll(allItems)
            return
        }

        val allItemsMap = allItems.associateBy { it.id }.toMutableMap()

        onScreenItems.forEach {
            allItemsMap.remove(it.id)
        }

        workingItems.addAll(allItemsMap.values.toList())
    }

    fun nextRandomItem(): T? {
        return try {
            val index = workingItems.indices.random()
            val item = workingItems[index]
            workingItems.removeAt(index)
            item
        } catch (ex: Exception) {
            null
        }
    }

    companion object {
        var set = mutableMapOf<GoogleMediaItem, Int>()

        fun getUnusedNext(thingsINeeds: List<GoogleMediaItem>): Int {

            if (set.size == thingsINeeds.size) {
                throw ArrayIndexOutOfBoundsException()
            }

            var index = thingsINeeds.indices.random()

            while (set.contains(thingsINeeds[index])) {
                index = thingsINeeds.indices.random()
            }

            set[thingsINeeds[index]] = index

            return index
        }

        fun reset(workingPhotoUrls: Queue<GoogleMediaItem>) {
            val tempSet = mutableMapOf<GoogleMediaItem, Int>()

            workingPhotoUrls.forEach {
                val index = set[it]
                tempSet[it] = index!!
            }

            set = tempSet
        }
    }
}
