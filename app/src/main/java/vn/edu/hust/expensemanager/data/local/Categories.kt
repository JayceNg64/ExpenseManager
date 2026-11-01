package vn.edu.hust.expensemanager.data.local

/** Danh mục cố định cho thu và chi, kèm biểu tượng emoji để hiển thị. */
object Categories {
    val EXPENSE = listOf(
        "Ăn uống", "Di chuyển", "Mua sắm", "Học tập",
        "Giải trí", "Nhà ở", "Sức khỏe", "Khác"
    )
    val INCOME = listOf(
        "Lương", "Học bổng", "Gia đình gửi", "Làm thêm", "Khác"
    )

    private val ICONS = mapOf(
        "Ăn uống" to "🍜", "Di chuyển" to "🛵", "Mua sắm" to "🛍️",
        "Học tập" to "📚", "Giải trí" to "🎮", "Nhà ở" to "🏠",
        "Sức khỏe" to "💊", "Lương" to "💼", "Học bổng" to "🎓",
        "Gia đình gửi" to "👨‍👩‍👧", "Làm thêm" to "🧑‍💻"
    )

    fun iconOf(category: String): String = ICONS[category] ?: "💰"
}
