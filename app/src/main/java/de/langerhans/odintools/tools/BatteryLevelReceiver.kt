package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.langerhans.odintools.data.SharedPrefsRepo
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelReceiver : BroadcastReceiver() {

    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var settings: SettingsRepo

    @Inject
    lateinit var prefs: SharedPrefsRepo

    override fun onReceive(context: Context, intent: Intent) {
        if (!prefs.chargeLimitEnabled || intent.action !in ALLOWED_INTENTS) {
            return
        }

        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        when (
            decideChargeAction(
                minLevel = prefs.minBatteryLevel,
                maxLevel = prefs.maxBatteryLevel,
                batteryLevel = batteryLevel,
                separationEnabled = settings.chargingSeparationEnabled(),
            )
        ) {
            ChargeAction.ENABLE_SEPARATION -> settings.enableChargingSeparation()
            ChargeAction.DISABLE_SEPARATION -> settings.disableChargingSeparation()
            ChargeAction.NONE -> Unit
        }

        // Reflect the resulting state in an ongoing notification so the (otherwise invisible)
        // charge-limit behaviour is explained while plugged in: charging up to the max, or held
        // at the limit and why. Recomputed after the action above so it shows the post-action truth.
        ChargeLimitNotifier.update(
            context = context,
            status = decideChargeStatus(
                plugged = isPlugged(context, intent),
                separationEnabled = settings.chargingSeparationEnabled(),
            ),
            batteryLevel = batteryLevel,
            minLevel = prefs.minBatteryLevel,
            maxLevel = prefs.maxBatteryLevel,
        )
    }

    private fun isPlugged(context: Context, intent: Intent): Boolean = when (intent.action) {
        Intent.ACTION_POWER_CONNECTED -> true
        Intent.ACTION_POWER_DISCONNECTED -> false
        else -> {
            // BATTERY_CHANGED carries EXTRA_PLUGGED directly; SCREEN_ON/OFF do not, so fall back
            // to the sticky battery intent to learn the current plug state.
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                .takeIf { it != -1 }
                ?: context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    ?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                ?: 0
            plugged != 0
        }
    }

    companion object {
        val ALLOWED_INTENTS = listOf(
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_SCREEN_ON,
        )
    }
}

enum class ChargeStatus { UNPLUGGED, CHARGING, PAUSED }

/**
 * Pure mapping from the current plug + separation state to what the user should be told while the
 * charge-limit feature is on. Unplugged means nothing to explain (the notification is cleared);
 * plugged with separation engaged means charging is held at the limit; otherwise it is charging.
 */
internal fun decideChargeStatus(plugged: Boolean, separationEnabled: Boolean): ChargeStatus = when {
    !plugged -> ChargeStatus.UNPLUGGED
    separationEnabled -> ChargeStatus.PAUSED
    else -> ChargeStatus.CHARGING
}

enum class ChargeAction { ENABLE_SEPARATION, DISABLE_SEPARATION, NONE }

/**
 * Pure decision for the auto charge-limit: at/above [maxLevel] turn charging separation on,
 * at/below [minLevel] turn it off. A misconfigured range where min >= max would flip separation
 * on/off on every battery broadcast, so it is treated as a no-op instead of thrashing the charger.
 */
internal fun decideChargeAction(minLevel: Int, maxLevel: Int, batteryLevel: Int, separationEnabled: Boolean): ChargeAction {
    if (minLevel >= maxLevel) return ChargeAction.NONE
    return when {
        batteryLevel >= maxLevel && !separationEnabled -> ChargeAction.ENABLE_SEPARATION
        batteryLevel <= minLevel && separationEnabled -> ChargeAction.DISABLE_SEPARATION
        else -> ChargeAction.NONE
    }
}
