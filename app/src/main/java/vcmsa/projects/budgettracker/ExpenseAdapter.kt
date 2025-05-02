package vcmsa.projects.budgettracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vcmsa.projects.budgettracker.R
import vcmsa.projects.budgettracker.model.ExpenseWithCategory
import java.text.SimpleDateFormat
import java.util.*

/*********
Title: <Android RecyclerView in Kotlin>
Author: <GeeksForGeeks>
Date: <2025>
Code version <1>
Availability:<https://www.geeksforgeeks.org/android-recyclerview-in-kotlin/>
 **********/
class ExpenseAdapter : ListAdapter<ExpenseWithCategory, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false) // Make sure item_expense.xml exists
        return ExpenseViewHolder(view)
    }


    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context

        holder.categoryTextView.text = item.category.name
        holder.amountTextView.text = "$${item.expense.amount}"
        holder.descriptionTextView.text = item.expense.description
        holder.dateTextView.text = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            .format(Date(item.expense.date))

        holder.paymentTextView.text = "Paid by: ${item.expense.paymentMethod}"
        holder.tagsTextView.text = "Tags: ${item.expense.tags ?: "None"}"
        holder.notesTextView.text = "Notes: ${item.expense.notes ?: "None"}"

        val lat = item.expense.latitude
        val lon = item.expense.longitude


        // Load receipt photo if available
        if (!item.expense.receiptPhoto.isNullOrEmpty()) {
            holder.receiptImageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(item.expense.receiptPhoto!!.toUri())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.receiptImageView)
        } else {
            holder.receiptImageView.visibility = View.GONE
        }
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.tvCategory)
        val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val paymentTextView: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        val tagsTextView: TextView = itemView.findViewById(R.id.tvTags)
        val notesTextView: TextView = itemView.findViewById(R.id.tvNotes)
        val receiptImageView: ImageView = itemView.findViewById(R.id.ivReceiptPhoto)
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<ExpenseWithCategory>() {
        override fun areItemsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory): Boolean {
            return oldItem.expense.id == newItem.expense.id
        }

        override fun areContentsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory): Boolean {
            return oldItem == newItem
        }
    }
}
