package vn.edu.hust.expensemanager.ui.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vn.edu.hust.expensemanager.R
import vn.edu.hust.expensemanager.data.local.Categories
import vn.edu.hust.expensemanager.data.local.Transaction
import vn.edu.hust.expensemanager.databinding.ItemTransactionBinding
import vn.edu.hust.expensemanager.util.toDateString
import vn.edu.hust.expensemanager.util.toVnd

/**
 * Adapter cho RecyclerView. ListAdapter + DiffUtil chỉ vẽ lại những dòng thay đổi.
 */
class TransactionAdapter(
    private val onClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: Transaction) {
            val ctx = b.root.context
            b.tvIcon.text = Categories.iconOf(t.category)
            b.tvCategory.text = t.category
            b.tvNote.text = t.note.ifBlank { ctx.getString(R.string.no_note) }
            b.tvDate.text = t.date.toDateString("dd/MM/yyyy")
            b.tvAmount.text = (if (t.isExpense) "-" else "+") + t.amount.toVnd()
            b.tvAmount.setTextColor(
                ContextCompat.getColor(ctx, if (t.isExpense) R.color.expense else R.color.income)
            )
            b.ivReceipt.visibility = if (t.imagePath != null) View.VISIBLE else View.GONE
            b.root.setOnClickListener { onClick(t) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
            override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
        }
    }
}
