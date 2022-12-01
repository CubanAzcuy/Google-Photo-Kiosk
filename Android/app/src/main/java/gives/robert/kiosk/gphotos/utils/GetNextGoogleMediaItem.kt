package gives.robert.kiosk.gphotos.utils

import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem
import java.util.*

class GetNextGoogleMediaItem {
    private var size = -1
    private var lastRandomIndexes: Queue<Int> = LinkedList()
    private val shuffleAmounts = mutableMapOf<Int, Int>()
    private val curentIndexesRandomIndex = mutableMapOf<Int, Int>()
    private val maxShuffleAmount = 12
    private var countOfFilledIndexes = 0

    fun nextRandom(range: IntRange, currentIndex: Int): Int {
        if (range.last != size || countOfFilledIndexes == size) {
            setup(range)
        }

        if(curentIndexesRandomIndex.contains(currentIndex)) {
            return curentIndexesRandomIndex[currentIndex]!!
        }

        var index = range.random()
        while (lastRandomIndexes.contains(index) && (shuffleAmounts.contains(index) && (shuffleAmounts[index]
                ?: 0) < maxShuffleAmount)
        ) {
            index = range.random()
        }

        shuffleAmounts[index] = (shuffleAmounts[index] ?: 0) + 1

        if (shuffleAmounts[index] == maxShuffleAmount) {
            countOfFilledIndexes++
        }

        lastRandomIndexes.add(index)
        if (lastRandomIndexes.size > size / 5) {
            lastRandomIndexes.remove()
        }

        curentIndexesRandomIndex[currentIndex] = index
        return index
    }

    private fun setup(range: IntRange) {
        lastRandomIndexes = LinkedList()
        size = range.last
        countOfFilledIndexes = 0
        shuffleAmounts.clear()
    }

    companion object {
        var set = mutableMapOf<GoogleMediaItem, Int>()

        fun getUnusedNext(thingsINeeds: List<GoogleMediaItem>): Int {

            if(set.size == thingsINeeds.size) {
                throw ArrayIndexOutOfBoundsException()
            }

            var index = thingsINeeds.indices.random()

            while(set.contains(thingsINeeds[index])) {
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
