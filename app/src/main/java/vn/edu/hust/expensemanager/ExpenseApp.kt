package vn.edu.hust.expensemanager

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import vn.edu.hust.expensemanager.data.local.AppDatabase
import vn.edu.hust.expensemanager.data.prefs.SettingsManager
import vn.edu.hust.expensemanager.data.repository.TransactionRepository
import vn.edu.hust.expensemanager.util.NotificationHelper
import vn.edu.hust.expensemanager.worker.ReminderScheduler

/**
 * Lớp Application: khởi tạo một lần khi ứng dụng chạy.
 * Đóng vai trò "service locator" đơn giản cho Repository và Settings.
 */
class ExpenseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
    val settings by lazy { SettingsManager(this) }

    override fun onCreate() {
        super.onCreate()
        // Áp dụng chế độ sáng/tối đã lưu
        AppCompatDelegate.setDefaultNightMode(
            if (settings.darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        // Tạo kênh thông báo (bắt buộc từ Android 8.0)
        NotificationHelper.createChannels(this)
        // Lên lịch nhắc nhở hằng ngày nếu người dùng bật
        if (settings.reminderEnabled) {
            ReminderScheduler.schedule(this, settings.reminderHour, settings.reminderMinute)
        }
    }
}
