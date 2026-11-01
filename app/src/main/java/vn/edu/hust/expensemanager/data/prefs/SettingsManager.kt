package vn.edu.hust.expensemanager.data.prefs

import android.content.Context
import androidx.core.content.edit

/**
 * Lưu cài đặt người dùng bằng SharedPreferences (dạng key-value).
 */
class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Hạn mức chi tiêu mỗi tháng (VND). 0 = không đặt hạn mức. */
    var monthlyBudget: Double
        get() = prefs.getFloat(KEY_BUDGET, 3_000_000f).toDouble()
        set(value) = prefs.edit { putFloat(KEY_BUDGET, value.toFloat()) }

    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK, false)
        set(value) = prefs.edit { putBoolean(KEY_DARK, value) }

    var reminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER, true)
        set(value) = prefs.edit { putBoolean(KEY_REMINDER, value) }

    var reminderHour: Int
        get() = prefs.getInt(KEY_HOUR, 21)
        set(value) = prefs.edit { putInt(KEY_HOUR, value) }

    var reminderMinute: Int
        get() = prefs.getInt(KEY_MINUTE, 0)
        set(value) = prefs.edit { putInt(KEY_MINUTE, value) }

    /** Tháng gần nhất đã gửi cảnh báo vượt hạn mức, tránh báo lặp lại (yyyyMM). */
    var lastBudgetAlertMonth: Int
        get() = prefs.getInt(KEY_ALERT_MONTH, 0)
        set(value) = prefs.edit { putInt(KEY_ALERT_MONTH, value) }

    companion object {
        private const val PREFS_NAME = "expense_settings"
        private const val KEY_BUDGET = "monthly_budget"
        private const val KEY_DARK = "dark_mode"
        private const val KEY_REMINDER = "reminder_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        private const val KEY_ALERT_MONTH = "last_budget_alert_month"
    }
}
