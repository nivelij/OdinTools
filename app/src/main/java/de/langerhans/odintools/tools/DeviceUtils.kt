package de.langerhans.odintools.tools

import de.langerhans.odintools.tools.DeviceType.ODIN2
import de.langerhans.odintools.tools.DeviceType.OTHER
import de.langerhans.odintools.tools.DeviceType.RP4
import javax.inject.Inject

class DeviceUtils @Inject constructor(
    private val executor: ShellExecutor,
) {

    fun getDeviceVersion(): String = executor.getStringProperty(SettingsRepo.KEY_BUILD_VERSION, "")
        // The Odin 2 Portal doesn't populate ro.build.odin2.ota.version; fall back to the
        // FOTA version property, which holds the firmware version there (e.g. "1.0.0.338").
        .ifEmpty { executor.getStringProperty(SettingsRepo.KEY_FOTA_VERSION, "") }

    fun getDeviceCodename() = executor.getStringProperty(SettingsRepo.KEY_VENDOR_NAME, "")

    fun getDeviceType(): DeviceType {
        return when (getDeviceCodename()) {
            "Q9" -> ODIN2
            "4.0", "4.0P" -> RP4
            else -> OTHER
        }
    }
}

enum class DeviceType {
    ODIN2,
    RP4,
    OTHER,
}
