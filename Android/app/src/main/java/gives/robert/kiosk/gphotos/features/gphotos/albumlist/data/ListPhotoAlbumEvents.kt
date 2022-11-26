package gives.robert.kiosk.gphotos.features.gphotos.albumlist.data

sealed interface ListPhotoAlbumEvents {
    object GetAlbums: ListPhotoAlbumEvents
    data class SelectAlbum(val selectedAlbumsId: String) : ListPhotoAlbumEvents
}