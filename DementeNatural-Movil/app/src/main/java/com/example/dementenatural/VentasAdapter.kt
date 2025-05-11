package com.example.dementenatural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VentasAdapter(private val ventasList: List<Sale>) :
    RecyclerView.Adapter<VentasAdapter.VentaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val sale = ventasList[position]
        holder.saleId.text = "Venta #${sale.saleId}"
        holder.saleAmount.text = "%,.2f COP".format(sale.total)
    }

    override fun getItemCount(): Int = ventasList.size

    inner class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val saleId: TextView = itemView.findViewById(R.id.saleId)
        val saleAmount: TextView = itemView.findViewById(R.id.saleAmount)
    }
}
