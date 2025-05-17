package com.example.dementenatural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SummaryItem(
    val id: String,
    val name: String,
    val qty: Int,
    val subtotal: Double
)

class SummaryAdapter(
    private val items: List<SummaryItem>
) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.summaryName)
        val tvQty: TextView = view.findViewById(R.id.summaryQty)
        val tvSub: TextView = view.findViewById(R.id.summaryPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary_product, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val it = items[position]
        holder.tvName.text = it.name
        holder.tvQty.text = "x${it.qty}"
        holder.tvSub.text = "%,.2f COP".format(it.subtotal)
    }

    override fun getItemCount() = items.size
}
