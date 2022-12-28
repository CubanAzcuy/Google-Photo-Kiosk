package gives.robert.kiosk.gphotos.utils

import kotlin.random.Random
import kotlin.random.nextInt

interface IdItem {
    val id: Any
}

class ItemHolderRandom<T : IdItem> {

    private val workingItems = mutableListOf<T>()
    private val random = Random(System.currentTimeMillis().toInt())

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
            val index = random.nextInt(workingItems.indices)
            val item = workingItems[index]
            workingItems.removeAt(index)
            item
        } catch (ex: Exception) {
            null
        }
    }
}
