package vn.edu.hust.expensemanager.util

import android.content.Context
import vn.edu.hust.expensemanager.ExpenseApp

/**
 * Kiểm tra tổng chi tiêu tháng hiện tại so với hạn mức.
 * Chỉ gửi cảnh báo một lần mỗi tháng.
 */
object BudgetChecker {

    suspend fun check(context: Context) {
        val app = context.applicationContext as ExpenseApp
        val budget = app.settings.monthlyBudget
        if (budget <= 0) return

        val (from, to) = DateRange.monthOf()
        val spent = app.repository.monthExpenseTotal(from, to)
        val monthKey = DateRange.monthKey()

        if (spent > budget && app.settings.lastBudgetAlertMonth != monthKey) {
            NotificationHelper.showBudgetAlert(context, spent, budget)
            app.settings.lastBudgetAlertMonth = monthKey
        }
    }
}
