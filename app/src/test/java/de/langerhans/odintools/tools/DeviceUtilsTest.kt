package de.langerhans.odintools.tools

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceUtilsTest {

    private val executor: ShellExecutor = mockk()
    private val deviceUtils = DeviceUtils(executor)

    @Test
    fun `getDeviceVersion uses the Odin 2 OTA version when present`() {
        every { executor.getStringProperty(SettingsRepo.KEY_BUILD_VERSION, "") } returns "1.0.0.288"

        assertEquals("1.0.0.288", deviceUtils.getDeviceVersion())
    }

    @Test
    fun `getDeviceVersion falls back to the FOTA version when the OTA version is empty (Portal)`() {
        every { executor.getStringProperty(SettingsRepo.KEY_BUILD_VERSION, "") } returns ""
        every { executor.getStringProperty(SettingsRepo.KEY_FOTA_VERSION, "") } returns "1.0.0.338"

        assertEquals("1.0.0.338", deviceUtils.getDeviceVersion())
    }
}
