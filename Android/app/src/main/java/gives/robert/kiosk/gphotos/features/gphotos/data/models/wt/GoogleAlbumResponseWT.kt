package gives.robert.kiosk.gphotos.features.gphotos.data.models.wt

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAlbumResponseWT(
    var id: String,
    var title: String,
    var productUrl: String,
    var mediaItemsCount: String,
    var coverPhotoBaseUrl: String,
    var coverPhotoMediaItemId: String
)
