package vn.edu.hust.expensemanager.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.data.local.CategoryTotal
import vn.edu.hust.expensemanager.data.local.DailyTotal
import vn.edu.hust.expensemanager.databinding.FragmentStatsBinding
import vn.edu.hust.expensemanager.ui.MainViewModel
import vn.edu.hust.expensemanager.util.toDateString
import vn.edu.hust.expensemanager.util.toVnd

/**
 * Màn hình Thống kê: biểu đồ tròn chi theo danh mục và biểu đồ cột chi theo ngày.
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val vm: MainViewModel by activityViewModels()

    private val palette = listOf(
        "#2563EB", "#F97316", "#10B981", "#A855F7",
        "#EF4444", "#0EA5E9", "#EAB308", "#64748B"
    ).map { Color.parseColor(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupCharts()
        binding.monthSelector.btnPrevMonth.setOnClickListener { vm.previousMonth() }
        binding.monthSelector.btnNextMonth.setOnClickListener { vm.nextMonth() }
        vm.month.observe(viewLifecycleOwner) {
            binding.monthSelector.tvMonth.text = getString(R.string.month_label, it.toDateString("MM/yyyy"))
        }
        vm.monthExpense.observe(viewLifecycleOwner) {
            binding.tvTotal.text = getString(R.string.total_expense, it.toVnd())
        }
        vm.categoryTotals.observe(viewLifecycleOwner) { showPie(it) }
        vm.dailyTotals.observe(viewLifecycleOwner) { showBar(it) }
    }

    private fun textColor() = ContextCompat.getColor(requireContext(), R.color.text_primary)

    private fun setupCharts() = with(binding) {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.holeRadius = 55f
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.legend.textColor = textColor()
        pieChart.legend.isWordWrapEnabled = true
        pieChart.setNoDataText(getString(R.string.no_data))

        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.textColor = textColor()
        barChart.axisLeft.textColor = textColor()
        barChart.axisLeft.axisMinimum = 0f
        barChart.setNoDataText(getString(R.string.no_data))
    }

    private fun showPie(list: List<CategoryTotal>) {
        if (list.isEmpty()) { binding.pieChart.clear(); return }
        val entries = list.map { PieEntry(it.total.toFloat(), it.category) }
        val set = PieDataSet(entries, "").apply {
            colors = palette
            sliceSpace = 2f
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        binding.pieChart.data = PieData(set).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
        }
        binding.pieChart.centerText = getString(R.string.by_category)
        binding.pieChart.setCenterTextColor(textColor())
        binding.pieChart.animateY(600)
        binding.pieChart.invalidate()
    }

    private fun showBar(list: List<DailyTotal>) {
        if (list.isEmpty()) { binding.barChart.clear(); return }
        // Trục X hiển thị "dd/MM"; giá trị trục Y tính theo nghìn đồng cho gọn
        val labels = list.map { it.day.substring(8, 10) + "/" + it.day.substring(5, 7) }
        val entries = list.mapIndexed { i, d -> BarEntry(i.toFloat(), (d.total / 1000).toFloat()) }
        val set = BarDataSet(entries, getString(R.string.thousand_vnd)).apply {
            color = palette[0]
            valueTextColor = textColor()
        }
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.data = BarData(set).apply { barWidth = 0.6f }
        binding.barChart.animateY(600)
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
