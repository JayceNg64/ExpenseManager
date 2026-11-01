package vn.edu.hust.expensemanager.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng "transactions": mỗi dòng là một khoản thu hoặc chi.
 * type = 0: chi tiêu (EXPENSE), type = 1: thu nhập (INCOME)
 */
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["date"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "note") val note: String = "",
    @ColumnInfo(name = "date") val date: Long,             // epoch millis
    @ColumnInfo(name = "image_path") val imagePath: String? = null // ảnh hóa đơn
) {
    val isExpense: Boolean get() = type == TYPE_EXPENSE

    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_INCOME = 1
    }
}

/** Kết quả truy vấn gộp: tổng tiền theo danh mục (dùng cho biểu đồ tròn). */
data class CategoryTotal(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "total") val total: Double
)

/** Kết quả truy vấn gộp: tổng tiền theo ngày (dùng cho biểu đồ cột). */
data class DailyTotal(
    @ColumnInfo(name = "day") val day: String,   // định dạng yyyy-MM-dd
    @ColumnInfo(name = "total") val total: Double
)
