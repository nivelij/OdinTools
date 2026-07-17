package de.langerhans.odintools.tools

import org.junit.Assert.assertEquals
import org.junit.Test

class DecideChargeActionTest {

    @Test
    fun `enables separation at or above max when currently off`() {
        assertEquals(
            ChargeAction.ENABLE_SEPARATION,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 80, separationEnabled = false),
        )
        assertEquals(
            ChargeAction.ENABLE_SEPARATION,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 95, separationEnabled = false),
        )
    }

    @Test
    fun `does not re-enable separation when already on`() {
        assertEquals(
            ChargeAction.NONE,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 90, separationEnabled = true),
        )
    }

    @Test
    fun `disables separation at or below min when currently on`() {
        assertEquals(
            ChargeAction.DISABLE_SEPARATION,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 20, separationEnabled = true),
        )
        assertEquals(
            ChargeAction.DISABLE_SEPARATION,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 5, separationEnabled = true),
        )
    }

    @Test
    fun `does nothing between min and max`() {
        assertEquals(
            ChargeAction.NONE,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 50, separationEnabled = false),
        )
        assertEquals(
            ChargeAction.NONE,
            decideChargeAction(minLevel = 20, maxLevel = 80, batteryLevel = 50, separationEnabled = true),
        )
    }

    @Test
    fun `does not thrash when min equals max`() {
        assertEquals(
            ChargeAction.NONE,
            decideChargeAction(minLevel = 50, maxLevel = 50, batteryLevel = 50, separationEnabled = false),
        )
        assertEquals(
            ChargeAction.NONE,
            decideChargeAction(minLevel = 50, maxLevel = 50, batteryLevel = 50, separationEnabled = true),
        )
    }

    @Test
    fun `treats inverted range as no-op`() {
        assertEquals(
            ChargeAction.NONE,
            decideChargeAction(minLevel = 80, maxLevel = 20, batteryLevel = 90, separationEnabled = false),
        )
    }
}
