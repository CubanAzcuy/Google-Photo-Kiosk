package gives.robert.kiosk.gphotos.features.display.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaItems(
    var id: String,
    var productUrl: String,
    var baseUrl: String,
    var mimeType: String,
    var mediaMetadata: MediaMetadata? = MediaMetadata(),
    var filename: String
)
