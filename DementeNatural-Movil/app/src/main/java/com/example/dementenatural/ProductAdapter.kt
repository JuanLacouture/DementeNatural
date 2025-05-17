package com.example.dementenatural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var products: List<Product>,
    private val selectedProducts: MutableMap<String, Int>, // <-- Aquí pasas el mapa desde la actividad
    private val onQuantityChange: (String, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    var currentProducts = products
        private set

    fun updateProducts(newProducts: List<Product>) {
        currentProducts = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentProducts[position])
    }

    override fun getItemCount() = currentProducts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.productName)
        private val price: TextView = itemView.findViewById(R.id.productPrice)
        private val decrement: TextView = itemView.findViewById(R.id.decrementButton)
        private val increment: TextView = itemView.findViewById(R.id.incrementButton)
        private val quantity: TextView = itemView.findViewById(R.id.quantityText)

        fun bind(product: Product) {
            name.text = product.name
            price.text = "%,.2f COP".format(product.price)
            quantity.text = (selectedProducts[product.id] ?: 0).toString()

            decrement.setOnClickListener {
                val currentQty = selectedProducts[product.id] ?: 0
                if (currentQty > 0) {
                    val newQty = currentQty - 1
                    selectedProducts[product.id] = newQty
                    quantity.text = newQty.toString()
                    onQuantityChange(product.id, newQty)
                }
            }

            increment.setOnClickListener {
                val currentQty = selectedProducts[product.id] ?: 0
                val newQty = currentQty + 1
                selectedProducts[product.id] = newQty
                quantity.text = newQty.toString()
                onQuantityChange(product.id, newQty)
            }
        }
    }
}
