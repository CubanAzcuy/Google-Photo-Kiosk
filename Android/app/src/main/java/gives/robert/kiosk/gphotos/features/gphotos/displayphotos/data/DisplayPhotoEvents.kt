package gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data

sealed interface DisplayPhotoEvents {
    object GetPhotos: DisplayPhotoEvents
    data class ScrolledBack(val currentIndex: Int): DisplayPhotoEvents
}