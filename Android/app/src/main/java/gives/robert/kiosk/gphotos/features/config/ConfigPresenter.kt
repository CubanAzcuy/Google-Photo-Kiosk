package gives.robert.kiosk.gphotos.features.config

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import gives.robert.kiosk.gphotos.features.config.data.ConfigureKioskEffect
import gives.robert.kiosk.gphotos.features.config.data.ConfigureKioskEvents
import gives.robert.kiosk.gphotos.features.config.data.ConfigureKioskState
import gives.robert.kiosk.gphotos.features.config.networking.AuthRepository
import gives.robert.kiosk.gphotos.utils.BasePresenter

class ConfigPresenter(private val authRepo: AuthRepository) :
    BasePresenter<ConfigureKioskEvents, ConfigureKioskState, ConfigureKioskEffect>() {

    override val baseState: ConfigureKioskState
        get() = ConfigureKioskState()

    override suspend fun handleEvent(event: ConfigureKioskEvents) {
        when (event) {
            is ConfigureKioskEvents.TokenFetched -> {
                processToken(event.googleAccount)
            }
        }
    }

    private suspend fun processToken(acct: GoogleSignInAccount) {
        val serverAuthCode = acct.serverAuthCode ?: return
        authRepo.authenticate(serverAuthCode)
    }
}
