package vn.edu.hust.expensemanager.data.repository

import androidx.lifecycle.LiveData
import vn.edu.hust.expensemanager.data.local.CategoryTotal
import vn.edu.hust.expensemanager.data.local.DailyTotal
import vn.edu.hust.expensemanager.data.local.Transaction
import vn.edu.hust.expensemanager.data.local.TransactionDao
import vn.edu.hust.expensemanager.data.remote.ExchangeRateResponse
import vn.edu.hust.expensemanager.data.remote.RetrofitClient

/**
 * Repository: lớp trung gian duy nhất mà ViewModel làm việc cùng.
 * Che giấu nguồn dữ liệu (Room cục bộ hoặc REST API từ xa).
 */
class TransactionRepository(private val dao: TransactionDao) {

    val allTransactions: LiveData<List<Transaction>> = dao.getAll()

    fun search(keyword: String) = dao.search(keyword)

    fun between(from: Long, to: Long) = dao.getBetween(from, to)

    fun sumExpense(from: Long, to: Long) = dao.sumByType(Transaction.TYPE_EXPENSE, from, to)

    fun sumIncome(from: Long, to: Long) = dao.sumByType(Transaction.TYPE_INCOME, from, to)

    fun expenseByCategory(from: Long, to: Long): LiveData<List<CategoryTotal>> =
        dao.expenseByCategory(from, to)

    fun expenseByDay(from: Long, to: Long): LiveData<List<DailyTotal>> =
        dao.expenseByDay(from, to)

    suspend fun getById(id: Long) = dao.getById(id)

    suspend fun insert(t: Transaction) = dao.insert(t)

    suspend fun update(t: Transaction) = dao.update(t)

    suspend fun delete(t: Transaction) = dao.delete(t)

    suspend fun monthExpenseTotal(from: Long, to: Long) =
        dao.sumByTypeOnce(Transaction.TYPE_EXPENSE, from, to)

    suspend fun monthIncomeTotal(from: Long, to: Long) =
        dao.sumByTypeOnce(Transaction.TYPE_INCOME, from, to)

    suspend fun listBetween(from: Long, to: Long) = dao.getBetweenOnce(from, to)

    suspend fun countBetween(from: Long, to: Long) = dao.countBetween(from, to)

    /** Gọi API tỷ giá (chạy trong coroutine, Retrofit tự chuyển sang luồng nền). */
    suspend fun fetchRates(base: String): ExchangeRateResponse =
        RetrofitClient.api.getLatest(base)
}
