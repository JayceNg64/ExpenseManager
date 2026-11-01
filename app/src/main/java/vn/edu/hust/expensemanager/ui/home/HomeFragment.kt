package vn.edu.hust.expensemanager.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.data.local.Transaction
import vn.edu.hust.expensemanager.databinding.FragmentHomeBinding
import vn.edu.hust.expensemanager.ui.MainViewModel
import vn.edu.hust.expensemanager.ui.add.AddEditTransactionActivity
import vn.edu.hust.expensemanager.ui.currency.CurrencyActivity
import vn.edu.hust.expensemanager.ui.transactions.TransactionAdapter
import vn.edu.hust.expensemanager.util.toDateString
import vn.edu.hust.expensemanager.util.toVnd

/**
 * Màn hình Tổng quan: số dư tháng, tổng thu/chi, tiến độ ngân sách,
 * 5 giao dịch gần nhất, lối tắt quy đổi tỷ giá và chia sẻ báo cáo.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: MainViewModel by activityViewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TransactionAdapter { openEdit(it) }
        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter

        binding.monthSelector.btnPrevMonth.setOnClickListener { vm.previousMonth() }
        binding.monthSelector.btnNextMonth.setOnClickListener { vm.nextMonth() }

        vm.month.observe(viewLifecycleOwner) {
            binding.monthSelector.tvMonth.text = getString(R.string.month_label, it.toDateString("MM/yyyy"))
        }
        vm.monthIncome.observe(viewLifecycleOwner) { binding.tvIncome.text = it.toVnd() }
        vm.monthExpense.observe(viewLifecycleOwner) { updateBudget(it) }
        vm.balance.observe(viewLifecycleOwner) { binding.tvBalance.text = it.toVnd() }

        vm.monthTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.take(5))
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnCurrency.setOnClickListener {
            startActivity(Intent(requireContext(), CurrencyActivity::class.java))
        }
        binding.btnShare.setOnClickListener { shareReport() }
    }

    override fun onResume() {
        super.onResume()
        // Hạn mức có thể vừa đổi ở màn hình Cài đặt
        updateBudget(vm.monthExpense.value ?: 0.0)
    }

    private fun updateBudget(spent: Double) {
        binding.tvExpense.text = spent.toVnd()
        val budget = vm.budget
        if (budget <= 0) {
            binding.cardBudget.visibility = View.GONE
            return
        }
        binding.cardBudget.visibility = View.VISIBLE
        val percent = ((spent / budget) * 100).toInt()
        binding.progressBudget.progress = percent.coerceAtMost(100)
        binding.tvBudgetPercent.text = getString(R.string.budget_percent, percent)
        binding.tvBudgetDetail.text =
            getString(R.string.budget_detail, spent.toVnd(), budget.toVnd())
        val color = when {
            percent >= 100 -> R.color.expense
            percent >= 80 -> R.color.warning
            else -> R.color.income
        }
        binding.progressBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun openEdit(t: Transaction) {
        startActivity(
            Intent(requireContext(), AddEditTransactionActivity::class.java)
                .putExtra(AddEditTransactionActivity.EXTRA_ID, t.id)
        )
    }

    /** Intent ngầm định ACTION_SEND: để hệ thống chọn ứng dụng chia sẻ (Zalo, Gmail...). */
    private fun shareReport() {
        val month = vm.month.value?.toDateString("MM/yyyy") ?: ""
        val text = buildString {
            appendLine(getString(R.string.share_title, month))
            appendLine("• Thu nhập: ${(vm.monthIncome.value ?: 0.0).toVnd()}")
            appendLine("• Chi tiêu: ${(vm.monthExpense.value ?: 0.0).toVnd()}")
            appendLine("• Số dư: ${(vm.balance.value ?: 0.0).toVnd()}")
            vm.categoryTotals.value?.takeIf { it.isNotEmpty() }?.let { list ->
                appendLine("Chi theo danh mục:")
                list.forEach { appendLine("  - ${it.category}: ${it.total.toVnd()}") }
            }
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title, month))
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_report)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
