package gives.robert.kiosk.gphotos.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class AlbumListResponse(
  var albums: List<Albums>,
  var nextPageToken: String? = null
)