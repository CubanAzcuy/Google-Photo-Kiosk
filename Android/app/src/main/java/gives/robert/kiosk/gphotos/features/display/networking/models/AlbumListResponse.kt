package gives.robert.kiosk.gphotos.features.display.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class AlbumListResponse(
    var albums: List<Albums>,
    var nextPageToken: String? = null
)