package gives.robert.kiosk.gphotos.features.config.ui.io

sealed interface ConfigureKioskEffect {
    object RequestToken : ConfigureKioskEffect
    object None: ConfigureKioskEffect
}
