package de.langerhans.odintools.appsettings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import de.langerhans.odintools.data.AppOverrideEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppOverrideMapperTest {

    private val packageManager: PackageManager = mockk()
    private val context: Context = mockk {
        every { packageManager } returns this@AppOverrideMapperTest.packageManager
    }
    private val mapper = AppOverrideMapper(context)

    private fun appInfo(pkg: String, system: Boolean = false, isEnabled: Boolean = true) = ApplicationInfo().apply {
        packageName = pkg
        flags = if (system) ApplicationInfo.FLAG_SYSTEM else 0
        enabled = isEnabled
    }

    private fun entity(pkg: String) = AppOverrideEntity(pkg, null, null, null, null)

    @Test
    fun `mapEmptyOverride returns null when package cannot be resolved`() {
        every { packageManager.getApplicationInfo("gone.pkg", any<Int>()) } throws
            PackageManager.NameNotFoundException()

        assertNull(mapper.mapEmptyOverride("gone.pkg"))
    }

    @Test
    fun `mapEmptyOverride returns model when package resolves`() {
        val info = appInfo("good.pkg")
        every { packageManager.getApplicationInfo("good.pkg", any<Int>()) } returns info
        every { packageManager.getApplicationLabel(info) } returns "Good"
        every { packageManager.getApplicationIcon(info) } returns mockk<Drawable>()

        val model = mapper.mapEmptyOverride("good.pkg")

        assertEquals("good.pkg", model?.packageName)
        assertEquals("Good", model?.appName)
    }

    @Test
    fun `mapOverrideCandidates skips apps whose resources fail to load`() {
        val good = appInfo("good.pkg")
        val bad = appInfo("bad.pkg")
        every { packageManager.getInstalledApplications(any<Int>()) } returns listOf(bad, good)
        every { packageManager.getApplicationLabel(good) } returns "Good"
        every { packageManager.getApplicationIcon(good) } returns mockk<Drawable>()
        every { packageManager.getApplicationLabel(bad) } returns "Bad"
        every { packageManager.getApplicationIcon(bad) } throws PackageManager.NameNotFoundException()

        val result = mapper.mapOverrideCandidates(emptyList())

        assertEquals(listOf("good.pkg"), result.map { it.packageName })
    }

    @Test
    fun `mapOverrideCandidates excludes system, disabled and already-overridden apps`() {
        val user = appInfo("user.pkg")
        val system = appInfo("system.pkg", system = true)
        val disabled = appInfo("disabled.pkg", isEnabled = false)
        val existing = appInfo("existing.pkg")
        every { packageManager.getInstalledApplications(any<Int>()) } returns
            listOf(user, system, disabled, existing)
        every { packageManager.getApplicationLabel(user) } returns "User"
        every { packageManager.getApplicationIcon(user) } returns mockk<Drawable>()

        val result = mapper.mapOverrideCandidates(listOf(entity("existing.pkg")))

        assertEquals(listOf("user.pkg"), result.map { it.packageName })
    }
}
