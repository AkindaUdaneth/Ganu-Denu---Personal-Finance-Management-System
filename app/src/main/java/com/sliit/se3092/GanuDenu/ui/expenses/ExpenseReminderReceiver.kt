package com.sliit.se3092.GanuDenu.ui.expenses

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sliit.se3092.GanuDenu.MainActivity
import com.sliit.se3092.ganudenu.R
import java.util.Calendar

class ExpenseReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Recurring Expense Due"
        val amount = intent.getDoubleExtra("EXTRA_AMOUNT", 0.0)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "expense_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for recurring bills and subscriptions"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or Intent.FILL_IN_ACTION or PendingIntent.FLAG_IMMUTABLE
        )

        val amountText = if (amount > 0) " (LKR ${amount.toInt()})" else ""
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Bill Reminder: $title")
            .setContentText("Your subscription or bill '$title'$amountText is due today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(title.hashCode(), notification)
        } catch (_: Exception) {
            // Permission catch
        }
    }
}

fun scheduleRecurringExpenseReminder(
    context: Context,
    title: String,
    amount: Double,
    dueDay: Int
) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ExpenseReminderReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_AMOUNT", amount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            title.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            val targetDay = dueDay.coerceIn(1, 31)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(maxDay))
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.MONTH, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 30,
            pendingIntent
        )
    } catch (_: Exception) {
        // Safe fallback
    }
}
