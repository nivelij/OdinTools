package de.langerhans.odintools.tools

import org.junit.Assert.assertEquals
import org.junit.Test

class DecideChargeStatusTest {

    @Test
    fun `unplugged is always UNPLUGGED regardless of separation`() {
        assertEquals(
            ChargeStatus.UNPLUGGED,
            decideChargeStatus(plugged = false, separationEnabled = false),
        )
        assertEquals(
            ChargeStatus.UNPLUGGED,
            decideChargeStatus(plugged = false, separationEnabled = true),
        )
    }

    @Test
    fun `plugged with separation engaged is PAUSED`() {
        assertEquals(
            ChargeStatus.PAUSED,
            decideChargeStatus(plugged = true, separationEnabled = true),
        )
    }

    @Test
    fun `plugged without separation is CHARGING`() {
        assertEquals(
            ChargeStatus.CHARGING,
            decideChargeStatus(plugged = true, separationEnabled = false),
        )
    }
}
