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
 * Posts a single, silent, low-importance notification reflecting the current charge-limit state.
 * A fixed notification id means each transition updates the same notification in place instead of
 * stacking. Requires POST_NOTIFICATIONS, which the app self-grants in
 * [SettingsRepo.applyRequiredSettings]; if it is missing, [NotificationManager.notify] simply
 * no-ops.
 */
object ChargeLimitNotifier {

    private const val CHANNEL_ID = "charge_limit"
    private const val NOTIFICATION_ID = 1001

    fun notifySeparationEnabled(context: Context, batteryLevel: Int) = post(
        context,
        title = context.getString(R.string.chargeLimitNotificationPausedTitle),
        text = context.getString(R.string.chargeLimitNotificationPausedText, batteryLevel),
    )

    fun notifySeparationDisabled(context: Context, batteryLevel: Int) = post(
        context,
        title = context.getString(R.string.chargeLimitNotificationResumedTitle),
        text = context.getString(R.string.chargeLimitNotificationResumedText, batteryLevel),
    )

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
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
