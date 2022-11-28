package gives.robert.kiosk.gphotos.features.gphotos.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaItemSearchRequest(
    val albumId: String,
    val pageSize: Int)
