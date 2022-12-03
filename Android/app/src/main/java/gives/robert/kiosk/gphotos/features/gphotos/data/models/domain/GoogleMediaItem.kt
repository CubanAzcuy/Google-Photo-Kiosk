package gives.robert.kiosk.gphotos.features.gphotos.data.models.domain

import gives.robert.kiosk.gphotos.utils.IdItem

@kotlinx.serialization.Serializable
data class GoogleMediaItem(
    override val id: String,
    val baseUrl: String,
    val mimeType: String,
    val albumId: String
): IdItem