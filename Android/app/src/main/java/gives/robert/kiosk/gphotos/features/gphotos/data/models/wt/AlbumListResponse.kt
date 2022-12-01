package gives.robert.kiosk.gphotos.features.gphotos.data.models.wt

import kotlinx.serialization.Serializable

@Serializable
data class AlbumListResponse(
    var albums: List<GoogleAlbumResponseWT>,
    var nextPageToken: String? = null
)