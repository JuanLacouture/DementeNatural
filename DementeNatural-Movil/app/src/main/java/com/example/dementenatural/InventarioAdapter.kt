package com.example.dementenatural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class InventarioAdapter(
    private val items: List<Product>
) : RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    inner class InventarioViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView        = view.findViewById(R.id.productName)
        val quantity: TextView    = view.findViewById(R.id.productQuantity)
        val price: TextView       = view.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_consultar, parent, false)
        // feedback táctil
        v.isClickable = true
        v.foreground = parent.context
            .obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            .getDrawable(0)
        return InventarioViewHolder(v)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val p = items[position]
        holder.name.text = p.name
        holder.quantity.text = "Cantidad: ${p.quantity}"
        val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
        holder.price.text = "Precio: ${nf.format(p.price)} COP"
    }

    override fun getItemCount(): Int = items.size
}
