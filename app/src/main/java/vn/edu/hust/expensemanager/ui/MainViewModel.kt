package vn.edu.hust.expensemanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vn.edu.hust.expensemanager.ExpenseApp
import vn.edu.hust.expensemanager.data.local.Transaction
import vn.edu.hust.expensemanager.util.DateRange
import java.util.Calendar

/**
 * ViewModel dùng chung cho các Fragment trong MainActivity
 * (Tổng quan, Giao dịch, Thống kê). Dữ liệu sống sót qua xoay màn hình.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExpenseApp
    private val repo = app.repository

    /** Tháng đang xem (một mốc thời gian bất kỳ trong tháng). */
    private val _month = MutableLiveData(System.currentTimeMillis())
    val month: LiveData<Long> = _month

    private val range: LiveData<Pair<Long, Long>> = _month.map { DateRange.monthOf(it) }

    val monthExpense: LiveData<Double> = range.switchMap { repo.sumExpense(it.first, it.second) }
    val monthIncome: LiveData<Double> = range.switchMap { repo.sumIncome(it.first, it.second) }
    val monthTransactions: LiveData<List<Transaction>> =
        range.switchMap { repo.between(it.first, it.second) }
    val categoryTotals = range.switchMap { repo.expenseByCategory(it.first, it.second) }
    val dailyTotals = range.switchMap { repo.expenseByDay(it.first, it.second) }

    /** Số dư = thu - chi, gộp từ hai LiveData. */
    val balance = MediatorLiveData<Double>().apply {
        fun update() { value = (monthIncome.value ?: 0.0) - (monthExpense.value ?: 0.0) }
        addSource(monthIncome) { update() }
        addSource(monthExpense) { update() }
    }

    /** Từ khóa tìm kiếm ở màn hình danh sách giao dịch. */
    private val query = MutableLiveData("")
    val searchResults: LiveData<List<Transaction>> = query.switchMap { q ->
        if (q.isNullOrBlank()) repo.allTransactions else repo.search(q.trim())
    }

    val budget: Double get() = app.settings.monthlyBudget

    fun setQuery(q: String) { query.value = q }

    fun previousMonth() = shiftMonth(-1)
    fun nextMonth() = shiftMonth(1)

    private fun shiftMonth(delta: Int) {
        val c = Calendar.getInstance().apply {
            timeInMillis = _month.value ?: System.currentTimeMillis()
            add(Calendar.MONTH, delta)
        }
        _month.value = c.timeInMillis
    }

    fun delete(t: Transaction) = viewModelScope.launch { repo.delete(t) }

    fun restore(t: Transaction) = viewModelScope.launch { repo.insert(t) }
}
