package de.langerhans.odintools.tools

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.langerhans.odintools.R
import de.langerhans.odintools.main.MainActivity

/**
 * Owns the single, silent, low-importance charge-limit notification. A fixed notification id means
 * every update replaces the same notification in place instead of stacking. While the charger is
 * connected it shows an ongoing notification explaining the charging state (topping up, or held at
 * the limit and why); once unplugged it is cleared. Requires POST_NOTIFICATIONS, which the app
 * self-grants in [SettingsRepo.applyRequiredSettings]; if it is missing, [NotificationManager]
 * calls simply no-op.
 */
object ChargeLimitNotifier {

    private const val CHANNEL_ID = "charge_limit"
    private const val NOTIFICATION_ID = 1001

    fun update(context: Context, status: ChargeStatus, batteryLevel: Int, minLevel: Int, maxLevel: Int) = when (status) {
        ChargeStatus.UNPLUGGED -> cancel(context)
        ChargeStatus.CHARGING -> post(
            context,
            title = context.getString(R.string.chargeLimitNotificationChargingTitle, maxLevel),
            text = context.getString(R.string.chargeLimitNotificationChargingText, batteryLevel, maxLevel),
        )
        ChargeStatus.PAUSED -> post(
            context,
            title = context.getString(R.string.chargeLimitNotificationPausedTitle, batteryLevel),
            text = context.getString(R.string.chargeLimitNotificationPausedText, minLevel, maxLevel),
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }

    private fun post(context: Context, title: String, text: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.chargeLimitNotificationChannelName),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_electrical_services)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
