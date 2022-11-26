package gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data

data class DisplayPhotosState(
    val photoUrls: List<Pair<String, String>> = emptyList(),
    val currentIndex: Int = 0
)
