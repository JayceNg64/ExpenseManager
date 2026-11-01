package vn.edu.hust.expensemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vn.edu.hust.expensemanager.data.prefs.SettingsManager
import vn.edu.hust.expensemanager.worker.ReminderScheduler

/**
 * BroadcastReceiver: nhận sự kiện hệ thống BOOT_COMPLETED sau khi điện thoại
 * khởi động lại, rồi lên lịch lại nhắc nhở hằng ngày theo cài đặt đã lưu.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val settings = SettingsManager(context)
        if (settings.reminderEnabled) {
            ReminderScheduler.schedule(context, settings.reminderHour, settings.reminderMinute)
        }
    }
}
