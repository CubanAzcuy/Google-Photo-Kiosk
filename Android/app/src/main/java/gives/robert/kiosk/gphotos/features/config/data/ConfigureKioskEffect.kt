package gives.robert.kiosk.gphotos.features.config.data

sealed interface ConfigureKioskEffect {
    object RequestToken : ConfigureKioskEffect
    object None: ConfigureKioskEffect
}
