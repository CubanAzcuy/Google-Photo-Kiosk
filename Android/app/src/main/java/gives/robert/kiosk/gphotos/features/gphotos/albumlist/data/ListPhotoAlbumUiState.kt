package gives.robert.kiosk.gphotos.features.gphotos.albumlist.data

import coil.request.ImageRequest


data class AlbumInfo(
    val url: String,
    val title: String,
    val id: String,
    val request: ImageRequest,
    val isSelected: Boolean)

data class ListPhotoAlbumUiState(
    val albums: List<AlbumInfo> = emptyList(),
)
