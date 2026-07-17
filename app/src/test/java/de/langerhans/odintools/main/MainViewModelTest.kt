package de.langerhans.odintools.main

import de.langerhans.odintools.data.SharedPrefsRepo
import de.langerhans.odintools.tools.DeviceType
import de.langerhans.odintools.tools.DeviceUtils
import de.langerhans.odintools.tools.SettingsRepo
import de.langerhans.odintools.tools.ShellExecutor
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelTest {

    private val deviceUtils: DeviceUtils = mockk(relaxed = true)
    private val executor: ShellExecutor = mockk(relaxed = true)
    private val settings: SettingsRepo = mockk(relaxed = true)
    private val prefs: SharedPrefsRepo = mockk(relaxed = true)

    private fun viewModel(): MainViewModel {
        every { deviceUtils.getDeviceType() } returns DeviceType.ODIN2
        every { deviceUtils.getDeviceVersion() } returns ""
        return MainViewModel(deviceUtils, executor, settings, prefs)
    }

    @Test
    fun `app overrides toggle reflects the saved preference when off`() {
        every { prefs.appOverridesEnabled } returns false

        assertFalse(viewModel().uiState.value.appOverridesEnabled)
    }

    @Test
    fun `app overrides toggle reflects the saved preference when on`() {
        every { prefs.appOverridesEnabled } returns true

        assertTrue(viewModel().uiState.value.appOverridesEnabled)
    }
}
