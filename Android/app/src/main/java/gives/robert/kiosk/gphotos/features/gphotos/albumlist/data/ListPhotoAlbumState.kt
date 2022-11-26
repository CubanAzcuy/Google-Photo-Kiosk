package gives.robert.kiosk.gphotos.features.gphotos.albumlist.data


data class AlbumInfo(val url: String, val title: String, val id: String, val isSelected: Boolean)

data class ListPhotoAlbumState(
    val albums: List<AlbumInfo> = emptyList(),
)
