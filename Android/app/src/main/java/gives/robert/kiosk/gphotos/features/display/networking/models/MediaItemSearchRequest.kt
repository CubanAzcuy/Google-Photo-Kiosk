package gives.robert.kiosk.gphotos.features.display.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaItemSearchRequest(val albumId: String, val pageSize: Int = 100)