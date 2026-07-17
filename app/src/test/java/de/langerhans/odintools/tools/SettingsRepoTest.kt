package de.langerhans.odintools.tools

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SettingsRepoTest {

    private val executor: ShellExecutor = mockk(relaxed = true)
    private val settings = SettingsRepo(executor)

    private fun setSeparated(active: Boolean) {
        every { executor.getBooleanSystemSetting(SettingsRepo.KEY_CHARGING_SEPARATION, any()) } returns active
        every { executor.getBooleanValue(SettingsRepo.KEY_RESTRICT_CHARGE, any()) } returns active
        every { executor.getIntValue(SettingsRepo.KEY_RESTRICT_CURRENT, any()) } returns if (active) 1000 else 1000000
    }

    @Test
    fun `disableChargingSeparationIfActive lifts separation when it is engaged`() {
        setSeparated(true)

        settings.disableChargingSeparationIfActive()

        verify { executor.setIntValue(SettingsRepo.KEY_RESTRICT_CURRENT, 1000000) }
        verify { executor.setBooleanValue(SettingsRepo.KEY_RESTRICT_CHARGE, false) }
        verify { executor.setBooleanSystemSetting(SettingsRepo.KEY_CHARGING_SEPARATION, false) }
    }

    @Test
    fun `disableChargingSeparationIfActive does nothing when not engaged`() {
        setSeparated(false)

        settings.disableChargingSeparationIfActive()

        verify(exactly = 0) { executor.setIntValue(SettingsRepo.KEY_RESTRICT_CURRENT, 1000000) }
        verify(exactly = 0) { executor.setBooleanValue(SettingsRepo.KEY_RESTRICT_CHARGE, false) }
    }
}
