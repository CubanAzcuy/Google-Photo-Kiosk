package gives.robert.kiosk.gphotos.features.gphotos.data.models.domain

@kotlinx.serialization.Serializable
data class GoogleMediaItem(
    val id: String,
    val baseUrl: String,
    val mimeType: String,
    val albumId: String
)