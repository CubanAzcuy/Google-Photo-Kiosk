package gives.robert.kiosk.gphotos.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(val access_token: String)
