package vn.edu.hust.expensemanager.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.ui.MainActivity
import vn.edu.hust.expensemanager.ui.add.AddEditTransactionActivity

/**
 * Tạo kênh thông báo và hiển thị thông báo.
 */
object NotificationHelper {

    const val CHANNEL_REMINDER = "reminder_channel"
    const val CHANNEL_BUDGET = "budget_channel"
    private const val ID_REMINDER = 1001
    private const val ID_BUDGET = 1002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                context.getString(R.string.channel_reminder),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.channel_reminder_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BUDGET,
                context.getString(R.string.channel_budget),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.channel_budget_desc) }
        )
    }

    /** Nhắc ghi chi tiêu; chạm vào sẽ mở thẳng màn hình thêm giao dịch. */
    fun showReminder(context: Context, todayCount: Int) {
        val intent = Intent(context, AddEditTransactionActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = if (todayCount == 0)
            context.getString(R.string.reminder_text_empty)
        else
            context.getString(R.string.reminder_text_count, todayCount)

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        notify(context, ID_REMINDER, notification)
    }

    /** Cảnh báo khi tổng chi trong tháng vượt hạn mức. */
    fun showBudgetAlert(context: Context, spent: Double, budget: Double) {
        val pending = PendingIntent.getActivity(
            context, 1, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = context.getString(R.string.budget_alert_text, spent.toVnd(), budget.toVnd())
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle(context.getString(R.string.budget_alert_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        notify(context, ID_BUDGET, notification)
    }

    private fun notify(context: Context, id: Int, n: android.app.Notification) {
        // Android 13+: cần quyền POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        NotificationManagerCompat.from(context).notify(id, n)
    }
}
