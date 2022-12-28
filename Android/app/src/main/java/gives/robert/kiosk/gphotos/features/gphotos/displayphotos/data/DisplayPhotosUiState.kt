package gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data

import gives.robert.kiosk.gphotos.features.gphotos.displayphotos.MediaItem

data class DisplayPhotosUiState(
    val photoUrls: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0
)
