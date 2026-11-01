package vn.edu.hust.expensemanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import vn.edu.hust.expensemanager.ExpenseApp
import vn.edu.hust.expensemanager.util.BudgetChecker
import vn.edu.hust.expensemanager.util.DateRange
import vn.edu.hust.expensemanager.util.NotificationHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Worker chạy nền mỗi ngày một lần:
 *  - đếm số giao dịch hôm nay và gửi thông báo nhắc nhở,
 *  - kiểm tra hạn mức ngân sách tháng.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as ExpenseApp
            val (from, to) = DateRange.today()
            val count = app.repository.countBetween(from, to)
            NotificationHelper.showReminder(applicationContext, count)
            BudgetChecker.check(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/** Lên lịch / hủy lịch nhắc nhở bằng WorkManager. */
object ReminderScheduler {

    private const val WORK_NAME = "daily_reminder"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** Số mili-giây từ bây giờ đến lần chạy kế tiếp lúc hour:minute. */
    private fun delayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
