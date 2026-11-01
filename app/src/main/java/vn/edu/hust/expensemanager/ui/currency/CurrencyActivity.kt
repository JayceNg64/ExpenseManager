package vn.edu.hust.expensemanager.ui.currency

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vn.edu.hust.expensemanager.ExpenseApp
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.databinding.ActivityCurrencyBinding
import vn.edu.hust.expensemanager.util.toVnd
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Quy đổi ngoại tệ sang VND.
 * Thể hiện: kiểm tra kết nối mạng, gọi REST API bằng Retrofit + Coroutine,
 * phân tích JSON, xử lý lỗi, hiển thị trạng thái đang tải.
 */
class CurrencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencyBinding
    private val repo by lazy { (application as ExpenseApp).repository }
    private val currencies = listOf("USD", "EUR", "JPY", "KRW", "CNY", "GBP", "SGD", "THB", "AUD")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.actvCurrency.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, currencies)
        )
        binding.actvCurrency.setText(currencies[0], false)
        binding.btnConvert.setOnClickListener { convert() }
    }

    private fun convert() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = getString(R.string.error_amount)
            return
        }
        binding.tilAmount.error = null

        if (!isOnline()) {
            showError(getString(R.string.error_no_network))
            return
        }

        val base = binding.actvCurrency.text.toString()
        binding.progress.visibility = View.VISIBLE
        binding.btnConvert.isEnabled = false
        binding.tvError.visibility = View.GONE

        // Coroutine gắn với vòng đời Activity: tự hủy khi Activity bị hủy
        lifecycleScope.launch {
            try {
                val res = repo.fetchRates(base)
                val rate = res.rates["VND"] ?: error("Không có tỷ giá VND")
                val nf = NumberFormat.getInstance(Locale("vi", "VN"))
                binding.tvResult.text = (amount * rate).toVnd()
                binding.tvRate.text = getString(R.string.rate_line, base, nf.format(rate))
                binding.tvUpdated.text = getString(R.string.updated_at, res.lastUpdate ?: "")
                binding.cardResult.visibility = View.VISIBLE
            } catch (e: Exception) {
                showError(getString(R.string.error_api, e.localizedMessage ?: ""))
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnConvert.isEnabled = true
            }
        }
    }

    private fun showError(msg: String) {
        binding.cardResult.visibility = View.GONE
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
