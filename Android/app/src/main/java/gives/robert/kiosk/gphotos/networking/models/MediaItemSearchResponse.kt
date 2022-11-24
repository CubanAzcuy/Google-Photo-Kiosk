package gives.robert.kiosk.gphotos.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaItemSearchResponse(
  var mediaItems: List<MediaItems>,
  var nextPageToken: String? = null
)