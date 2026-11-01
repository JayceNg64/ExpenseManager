package vn.edu.hust.expensemanager.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val VI = Locale("vi", "VN")

/** 1500000.0 -> "1.500.000 ₫" */
fun Double.toVnd(): String {
    val nf = NumberFormat.getInstance(VI)
    nf.maximumFractionDigits = 0
    return nf.format(this) + " ₫"
}

fun Long.toDateString(pattern: String = "dd/MM/yyyy"): String =
    SimpleDateFormat(pattern, VI).format(Date(this))

/** Mốc thời gian đầu và cuối của tháng chứa [time]. */
object DateRange {
    fun monthOf(time: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val c = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = c.timeInMillis
        c.add(Calendar.MONTH, 1)
        return start to c.timeInMillis - 1
    }

    fun today(): Pair<Long, Long> {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = c.timeInMillis
        return start to start + 24 * 60 * 60 * 1000 - 1
    }

    /** Khóa tháng dạng yyyyMM, ví dụ 202610. */
    fun monthKey(time: Long = System.currentTimeMillis()): Int {
        val c = Calendar.getInstance().apply { timeInMillis = time }
        return c.get(Calendar.YEAR) * 100 + c.get(Calendar.MONTH) + 1
    }
}
