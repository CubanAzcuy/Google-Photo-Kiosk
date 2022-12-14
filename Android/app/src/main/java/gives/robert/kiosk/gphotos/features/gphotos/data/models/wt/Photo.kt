package gives.robert.kiosk.gphotos.features.gphotos.data.models.wt

import kotlinx.serialization.Serializable

@Serializable
data class Photo(
    var cameraMake: String? = null,
    var cameraModel: String? = null,
    var focalLength: Double? = null,
    var apertureFNumber: Double? = null,
    var isoEquivalent: Long? = null,
    var exposureTime: String? = null
)
