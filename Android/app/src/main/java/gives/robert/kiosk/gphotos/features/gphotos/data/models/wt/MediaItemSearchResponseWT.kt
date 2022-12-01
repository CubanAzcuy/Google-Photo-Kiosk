package gives.robert.kiosk.gphotos.features.gphotos.data.models.wt

import kotlinx.serialization.Serializable

@Serializable
data class MediaItemSearchResponseWT(
    var mediaItems: List<MediaItems>,
    var nextPageToken: String? = null
)