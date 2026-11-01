package vn.edu.hust.expensemanager.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.databinding.FragmentTransactionsBinding
import vn.edu.hust.expensemanager.ui.MainViewModel
import vn.edu.hust.expensemanager.ui.add.AddEditTransactionActivity

/**
 * Màn hình danh sách tất cả giao dịch:
 *  - Tìm kiếm theo ghi chú/danh mục (SearchView)
 *  - Vuốt sang trái để xóa, có nút "Hoàn tác" (Snackbar)
 *  - Chạm để sửa
 */
class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = TransactionAdapter { t ->
            startActivity(
                Intent(requireContext(), AddEditTransactionActivity::class.java)
                    .putExtra(AddEditTransactionActivity.EXTRA_ID, t.id)
            )
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        vm.searchResults.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                vm.setQuery(q.orEmpty())
                return true
            }
        })

        // Vuốt để xóa
        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position < adapter.currentList.size) {
                    val item = adapter.currentList[position]
                    vm.delete(item)
                    Snackbar.make(binding.root, R.string.deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) { vm.restore(item) }
                        .show()
                }
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(binding.rvTransactions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
