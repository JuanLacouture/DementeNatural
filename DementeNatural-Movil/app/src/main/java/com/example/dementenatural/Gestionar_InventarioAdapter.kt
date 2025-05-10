package com.example.dementenatural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GestionarInventarioAdapter(
    private val items: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<GestionarInventarioAdapter.GestionarInventarioViewHolder>() {

    inner class GestionarInventarioViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView        = view.findViewById(R.id.productName)
        val quantity: TextView    = view.findViewById(R.id.productQuantity)
        val price: TextView       = view.findViewById(R.id.productPrice)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GestionarInventarioViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return GestionarInventarioViewHolder(v)
    }

    override fun onBindViewHolder(holder: GestionarInventarioViewHolder, position: Int) {
        val product = items[position]
        holder.name.text = product.name
        holder.quantity.text = "Cantidad: ${product.quantity}"
        holder.price.text = "Precio: ${product.price} COP"

        // Edit button click listener
        holder.editButton.setOnClickListener {
            onEditClick(product)
        }

        // Delete button click listener
        holder.deleteButton.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int = items.size
}
