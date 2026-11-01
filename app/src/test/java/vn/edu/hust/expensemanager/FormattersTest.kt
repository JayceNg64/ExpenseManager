package vn.edu.hust.expensemanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.edu.hust.expensemanager.util.DateRange
import vn.edu.hust.expensemanager.util.toVnd
import java.util.Calendar

/** Kiểm thử đơn vị (Unit Test) cho các hàm tiện ích, chạy trên JVM. */
class FormattersTest {

    @Test
    fun toVnd_formatsWithDotSeparator() {
        assertEquals("1.500.000 ₫", 1_500_000.0.toVnd())
        assertEquals("0 ₫", 0.0.toVnd())
    }

    @Test
    fun monthOf_returnsFirstAndLastMillisOfMonth() {
        val c = Calendar.getInstance().apply { set(2026, Calendar.FEBRUARY, 15, 10, 0) }
        val (start, end) = DateRange.monthOf(c.timeInMillis)

        val s = Calendar.getInstance().apply { timeInMillis = start }
        val e = Calendar.getInstance().apply { timeInMillis = end }
        assertEquals(1, s.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, s.get(Calendar.HOUR_OF_DAY))
        assertEquals(28, e.get(Calendar.DAY_OF_MONTH))   // 2026 không nhuận
        assertEquals(23, e.get(Calendar.HOUR_OF_DAY))
        assertTrue(end > start)
    }

    @Test
    fun monthKey_isYearTimes100PlusMonth() {
        val c = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 28) }
        assertEquals(202610, DateRange.monthKey(c.timeInMillis))
    }
}
