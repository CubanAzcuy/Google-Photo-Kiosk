package gives.robert.kiosk.gphotos.utils

import java.util.*

class BetterRandom {
    private var size = -1
    private var lastRandomIndexes: Queue<Int> = LinkedList()
    private val shuffleAmounts = mutableMapOf<Int, Int>()
    private val maxShuffleAmount = 12
    private var countOfFilledIndexes = 0

    fun nextRandom(range: IntRange): Int {
        if (range.last != size || countOfFilledIndexes == size) {
            setup(range)
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

        return index
    }

    private fun setup(range: IntRange) {
        lastRandomIndexes = LinkedList()
        size = range.last
        countOfFilledIndexes = 0
        shuffleAmounts.clear()
    }
}
