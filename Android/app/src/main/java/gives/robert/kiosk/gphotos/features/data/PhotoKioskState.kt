package gives.robert.kiosk.gphotos.features.data

data class PhotoKioskState(
    val photoUrls: List<Pair<String, String>> = emptyList()
)
