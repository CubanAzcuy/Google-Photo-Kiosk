package gives.robert.kiosk.gphotos.utils

import java.util.*

class BetterRandom {
    private var size = -1
    private var lastRandomIndexes: Queue<Int> = LinkedList()
    private val shuffleAmounts = mutableMapOf<Int, Int>()
    private val curentIndexsRandomIndex = mutableMapOf<Int, Int>()
    private val maxShuffleAmount = 12
    private var countOfFilledIndexes = 0

    fun nextRandom(range: IntRange, currentIndex: Int): Int {
        if (range.last != size || countOfFilledIndexes == size) {
            setup(range)
        }

        if(curentIndexsRandomIndex.contains(currentIndex)) {
            return curentIndexsRandomIndex[currentIndex]!!
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

        curentIndexsRandomIndex[currentIndex] = index
        return index
    }

    private fun setup(range: IntRange) {
        lastRandomIndexes = LinkedList()
        size = range.last
        countOfFilledIndexes = 0
        shuffleAmounts.clear()
    }

    companion object {
        val set = mutableSetOf<Int>()
        fun getUnusedNext(range: IntRange): Int {
            var index = range.random()

            while(set.contains(index)) {
                index = range.random()
            }
            return index
        }
    }
}
