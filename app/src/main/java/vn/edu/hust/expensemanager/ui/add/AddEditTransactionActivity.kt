package vn.edu.hust.expensemanager.ui.add

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.data.local.Categories
import vn.edu.hust.expensemanager.data.local.Transaction
import vn.edu.hust.expensemanager.databinding.ActivityAddEditBinding
import vn.edu.hust.expensemanager.util.toDateString
import java.io.File
import java.util.Calendar

/**
 * Màn hình Thêm / Sửa giao dịch.
 * Thể hiện: Form nhập liệu + kiểm tra dữ liệu, DatePicker, ChipGroup,
 * xin quyền Camera lúc chạy, chụp ảnh hóa đơn qua FileProvider, Dialog xác nhận xóa.
 */
class AddEditTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private val vm: AddEditViewModel by viewModels()
    private var pendingPhoto: File? = null

    // Kết quả chụp ảnh (Activity Result API)
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val file = pendingPhoto
            if (ok && file != null) {
                vm.imagePath = file.absolutePath
                showImage()
            } else {
                file?.delete()
            }
        }

    // Xin quyền Camera lúc chạy
    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else Toast.makeText(this, R.string.camera_denied, Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getLongExtra(EXTRA_ID, -1L)
        title = getString(if (id > 0) R.string.title_edit else R.string.title_add)

        if (id > 0) {
            vm.load(id)
            vm.loadedTransaction.observe(this) { t ->
                // Lần đầu: đổ dữ liệu vào form. Sau khi xoay màn hình: EditText tự giữ nội dung.
                if (t != null && savedInstanceState == null) fillForm(t) else bindState()
            }
        } else {
            bindState()
        }

        binding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val newType = if (checkedId == R.id.btnIncome) Transaction.TYPE_INCOME
            else Transaction.TYPE_EXPENSE
            if (newType != vm.type) {
                vm.type = newType
                vm.category = null
            }
            buildCategoryChips()
        }

        binding.etDate.setOnClickListener { pickDate() }
        binding.btnCamera.setOnClickListener { requestCamera() }
        binding.btnRemoveImage.setOnClickListener {
            vm.imagePath = null
            showImage()
        }
        binding.btnSave.setOnClickListener { save() }

        vm.saved.observe(this) { if (it) finish() }
    }

    private fun fillForm(t: Transaction) {
        binding.etAmount.setText(t.amount.toLong().toString())
        binding.etNote.setText(t.note)
        bindState()
        invalidateOptionsMenu()
    }

    /** Đồng bộ giao diện với trạng thái đang giữ trong ViewModel. */
    private fun bindState() {
        binding.toggleType.check(
            if (vm.type == Transaction.TYPE_INCOME) R.id.btnIncome else R.id.btnExpense
        )
        buildCategoryChips()
        binding.etDate.setText(vm.date.toDateString())
        showImage()
    }

    private fun buildCategoryChips() {
        val list = if (vm.type == Transaction.TYPE_INCOME) Categories.INCOME else Categories.EXPENSE
        binding.chipGroup.removeAllViews()
        list.forEach { name ->
            val chip = Chip(this).apply {
                text = "${Categories.iconOf(name)} $name"
                isCheckable = true
                isChecked = name == vm.category
                setOnClickListener { vm.category = name }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun pickDate() {
        val c = Calendar.getInstance().apply { timeInMillis = vm.date }
        DatePickerDialog(this, { _, y, m, d ->
            c.set(y, m, d)
            vm.date = c.timeInMillis
            binding.etDate.setText(vm.date.toDateString())
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) launchCamera()
        else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun launchCamera() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        pendingPhoto = file
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        takePicture.launch(uri)
    }

    private fun showImage() {
        val path = vm.imagePath
        if (path != null && File(path).exists()) {
            binding.ivReceipt.setImageURI(Uri.fromFile(File(path)))
            binding.ivReceipt.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
        } else {
            binding.ivReceipt.setImageDrawable(null)
            binding.ivReceipt.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
        }
    }

    /** Kiểm tra dữ liệu nhập trước khi lưu. */
    private fun save() {
        binding.tilAmount.error = null
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            binding.tilAmount.error = getString(R.string.error_amount)
            return
        }
        if (amount > 10_000_000_000.0) {
            binding.tilAmount.error = getString(R.string.error_amount_too_large)
            return
        }
        if (vm.category == null) {
            Toast.makeText(this, R.string.error_category, Toast.LENGTH_SHORT).show()
            return
        }
        vm.save(amount, binding.etNote.text.toString().trim())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (vm.editing != null) menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.action_delete -> {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.delete) { _, _ -> vm.delete() }
                .setNegativeButton(R.string.cancel, null)
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_ID = "extra_transaction_id"
    }
}
