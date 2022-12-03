package gives.robert.kiosk.gphotos.features.gphotos.displayphotos.data

sealed interface DisplayPhotoEvents {
    object GetPhotos : DisplayPhotoEvents
    object Scrolling : DisplayPhotoEvents
    object OnAuthLost : DisplayPhotoEvents
    data class ScrollingStopped(val currentIndex: Int) : DisplayPhotoEvents
}