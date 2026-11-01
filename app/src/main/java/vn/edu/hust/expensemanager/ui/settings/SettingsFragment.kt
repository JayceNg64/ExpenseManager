package vn.edu.hust.expensemanager.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.edu.hust.expensemanager.ExpenseApp
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.databinding.FragmentSettingsBinding
import vn.edu.hust.expensemanager.util.NotificationHelper
import vn.edu.hust.expensemanager.util.toVnd
import vn.edu.hust.expensemanager.worker.ReminderScheduler

/**
 * Màn hình Cài đặt: hạn mức ngân sách, chế độ tối, giờ nhắc nhở.
 * Mọi giá trị lưu bằng SharedPreferences (SettingsManager).
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settings by lazy { (requireActivity().application as ExpenseApp).settings }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refresh()

        binding.rowBudget.setOnClickListener { showBudgetDialog() }

        binding.switchDark.setOnCheckedChangeListener { _, checked ->
            if (checked == settings.darkMode) return@setOnCheckedChangeListener
            settings.darkMode = checked
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.switchReminder.setOnCheckedChangeListener { _, checked ->
            settings.reminderEnabled = checked
            if (checked) ReminderScheduler.schedule(requireContext(), settings.reminderHour, settings.reminderMinute)
            else ReminderScheduler.cancel(requireContext())
            binding.rowTime.isEnabled = checked
            binding.rowTime.alpha = if (checked) 1f else 0.4f
        }

        binding.rowTime.setOnClickListener {
            if (!settings.reminderEnabled) return@setOnClickListener
            TimePickerDialog(requireContext(), { _, h, m ->
                settings.reminderHour = h
                settings.reminderMinute = m
                ReminderScheduler.schedule(requireContext(), h, m)
                refresh()
            }, settings.reminderHour, settings.reminderMinute, true).show()
        }

        binding.btnTestNotify.setOnClickListener {
            NotificationHelper.showReminder(requireContext(), 0)
            Toast.makeText(requireContext(), R.string.notify_sent, Toast.LENGTH_SHORT).show()
        }
    }

    private fun refresh() {
        binding.tvBudgetValue.text =
            if (settings.monthlyBudget > 0) settings.monthlyBudget.toVnd()
            else getString(R.string.not_set)
        binding.switchDark.isChecked = settings.darkMode
        binding.switchReminder.isChecked = settings.reminderEnabled
        binding.tvTimeValue.text = "%02d:%02d".format(settings.reminderHour, settings.reminderMinute)
        binding.rowTime.alpha = if (settings.reminderEnabled) 1f else 0.4f
    }

    private fun showBudgetDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = getString(R.string.hint_budget)
            setText(settings.monthlyBudget.toLong().takeIf { it > 0 }?.toString() ?: "")
            setPadding(48, 32, 48, 32)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.monthly_budget)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                settings.monthlyBudget = input.text.toString().toDoubleOrNull() ?: 0.0
                settings.lastBudgetAlertMonth = 0 // cho phép cảnh báo lại với hạn mức mới
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
