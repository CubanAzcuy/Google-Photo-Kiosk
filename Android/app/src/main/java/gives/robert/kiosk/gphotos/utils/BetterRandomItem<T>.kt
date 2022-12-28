package gives.robert.kiosk.gphotos.utils

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
}
