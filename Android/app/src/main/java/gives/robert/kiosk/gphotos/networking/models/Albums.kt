package gives.robert.kiosk.gphotos.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class Albums(
    var id: String,
    var title: String,
    var productUrl: String,
    var mediaItemsCount: String,
    var coverPhotoBaseUrl: String,
    var coverPhotoMediaItemId: String
)
