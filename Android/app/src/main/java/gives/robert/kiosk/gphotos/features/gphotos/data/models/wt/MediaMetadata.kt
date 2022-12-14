package gives.robert.kiosk.gphotos.features.gphotos.data.models.wt

import kotlinx.serialization.Serializable

@Serializable
data class MediaMetadata(
    var creationTime: String? = null,
    var width: String? = null,
    var height: String? = null,
    var photo: Photo? = Photo()
)
