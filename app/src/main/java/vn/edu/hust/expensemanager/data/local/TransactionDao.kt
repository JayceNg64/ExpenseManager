package vn.edu.hust.expensemanager.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO (Data Access Object): khai báo các câu truy vấn SQL.
 * Room sinh mã cài đặt tự động lúc biên dịch (KSP).
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    /** Toàn bộ giao dịch, mới nhất trước. LiveData tự cập nhật UI khi dữ liệu đổi. */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): LiveData<List<Transaction>>

    /** Tìm kiếm theo ghi chú hoặc danh mục. */
    @Query(
        "SELECT * FROM transactions WHERE note LIKE '%' || :keyword || '%' " +
            "OR category LIKE '%' || :keyword || '%' ORDER BY date DESC"
    )
    fun search(keyword: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getBetween(from: Long, to: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun getBetweenOnce(from: Long, to: Long): List<Transaction>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date BETWEEN :from AND :to")
    fun sumByType(type: Int, from: Long, to: Long): LiveData<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date BETWEEN :from AND :to")
    suspend fun sumByTypeOnce(type: Int, from: Long, to: Long): Double

    @Query(
        "SELECT category, SUM(amount) AS total FROM transactions " +
            "WHERE type = 0 AND date BETWEEN :from AND :to " +
            "GROUP BY category ORDER BY total DESC"
    )
    fun expenseByCategory(from: Long, to: Long): LiveData<List<CategoryTotal>>

    @Query(
        "SELECT strftime('%Y-%m-%d', date / 1000, 'unixepoch', 'localtime') AS day, " +
            "SUM(amount) AS total FROM transactions " +
            "WHERE type = 0 AND date BETWEEN :from AND :to GROUP BY day ORDER BY day"
    )
    fun expenseByDay(from: Long, to: Long): LiveData<List<DailyTotal>>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :from AND :to")
    suspend fun countBetween(from: Long, to: Long): Int
}
