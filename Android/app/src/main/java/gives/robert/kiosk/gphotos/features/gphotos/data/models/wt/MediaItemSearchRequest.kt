package gives.robert.kiosk.gphotos.features.gphotos.data.models.wt

import kotlinx.serialization.Serializable

@Serializable
data class MediaItemSearchRequest(
    val albumId: String,
    val pageSize: Int,
    val pageToken: String?
)
