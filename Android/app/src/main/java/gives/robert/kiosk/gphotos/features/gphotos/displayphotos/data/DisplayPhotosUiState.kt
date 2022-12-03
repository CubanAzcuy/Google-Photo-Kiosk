package gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data

import gives.robert.kiosk.gphotos.features.gphotos.data.models.domain.GoogleMediaItem

data class DisplayPhotosUiState(
    val photoUrls: List<GoogleMediaItem> = emptyList(),
    val currentIndex: Int = 0
)
