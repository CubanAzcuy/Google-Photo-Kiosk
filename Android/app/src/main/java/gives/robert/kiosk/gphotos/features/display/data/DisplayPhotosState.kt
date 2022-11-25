package gives.robert.kiosk.gphotos.features.display.data

data class DisplayPhotosState(
    val photoUrls: List<Pair<String, String>> = emptyList(),
    val currentIndex: Int = 0
)
