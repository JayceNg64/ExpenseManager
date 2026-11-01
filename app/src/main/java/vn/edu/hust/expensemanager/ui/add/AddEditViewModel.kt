package vn.edu.hust.expensemanager.ui.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vn.edu.hust.expensemanager.ExpenseApp
import vn.edu.hust.expensemanager.data.local.Transaction
import vn.edu.hust.expensemanager.util.BudgetChecker

/**
 * ViewModel cho màn hình Thêm/Sửa giao dịch.
 * Giữ trạng thái form (loại, ngày, ảnh...) để không mất khi xoay màn hình.
 */
class AddEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as ExpenseApp).repository

    var editing: Transaction? = null
        private set
    var type: Int = Transaction.TYPE_EXPENSE
    var category: String? = null
    var date: Long = System.currentTimeMillis()
    var imagePath: String? = null
    var loaded = false

    private val _loadedTransaction = MutableLiveData<Transaction?>()
    val loadedTransaction: LiveData<Transaction?> = _loadedTransaction

    private val _saved = MutableLiveData<Boolean>()
    val saved: LiveData<Boolean> = _saved

    fun load(id: Long) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            val t = repo.getById(id)
            editing = t
            t?.let {
                type = it.type; category = it.category; date = it.date; imagePath = it.imagePath
            }
            _loadedTransaction.value = t
        }
    }

    fun save(amount: Double, note: String) {
        val t = Transaction(
            id = editing?.id ?: 0,
            amount = amount,
            type = type,
            category = category ?: "Khác",
            note = note,
            date = date,
            imagePath = imagePath
        )
        viewModelScope.launch {
            if (editing == null) repo.insert(t) else repo.update(t)
            if (t.isExpense) BudgetChecker.check(getApplication())
            _saved.value = true
        }
    }

    fun delete() {
        val t = editing ?: return
        viewModelScope.launch {
            repo.delete(t)
            _saved.value = true
        }
    }
}
