package vn.edu.hust.expensemanager.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.databinding.ActivityMainBinding
import vn.edu.hust.expensemanager.ui.add.AddEditTransactionActivity

/**
 * Activity chính: chứa NavHostFragment và thanh điều hướng dưới (Bottom Navigation)
 * với 4 Fragment: Tổng quan, Giao dịch, Thống kê, Cài đặt.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Xin quyền thông báo lúc chạy (Runtime Permission, Android 13+)
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* bỏ qua */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        val appBarConfig = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.transactionsFragment, R.id.statsFragment, R.id.settingsFragment)
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)

        // Nút "+" chỉ hiện ở 2 màn hình đầu
        navController.addOnDestinationChangedListener { _, dest, _ ->
            binding.fabAdd.visibility =
                if (dest.id == R.id.homeFragment || dest.id == R.id.transactionsFragment)
                    View.VISIBLE else View.GONE
        }

        // Intent tường minh: mở màn hình thêm giao dịch
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditTransactionActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onStart() {
        super.onStart()
        android.util.Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MainActivity" // theo dõi vòng đời Activity trong Logcat
    }
}
