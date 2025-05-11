package com.example.dementenatural

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Venta_Especifica : AppCompatActivity() {

    private lateinit var mDBRef: DatabaseReference

    private lateinit var titleSaleDetail: TextView
    private lateinit var buyerEmail: TextView
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var backButton: CardView

    private lateinit var saleId: String
    private lateinit var email: String
    private var productsMap: Map<String, Int> = emptyMap()
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_venta_especifica)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        mDBRef = FirebaseDatabase.getInstance().reference

        titleSaleDetail     = findViewById(R.id.titleSaleDetail)
        buyerEmail          = findViewById(R.id.buyerEmail)
        productsRecyclerView= findViewById(R.id.productsRecyclerView)
        totalAmount         = findViewById(R.id.totalAmount)
        backButton          = findViewById(R.id.backButton)

        // Recuperar datos del Intent
        saleId      = intent.getStringExtra("saleId") ?: ""
        email       = intent.getStringExtra("email")   ?: ""
        total       = intent.getDoubleExtra("total", 0.0)
        @Suppress("UNCHECKED_CAST")
        productsMap = intent.getSerializableExtra("products") as? Map<String,Int> ?: emptyMap()

        // Poner texto
        titleSaleDetail.text = "Venta #$saleId"
        buyerEmail     .text = "Venta hecha por: $email"
        totalAmount    .text = "%,.2f COP".format(total)

        // Preparar RecyclerView
        productsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar los detalles de cada producto
        loadSaleProducts()

        backButton.setOnClickListener { finish() }
    }

    private fun loadSaleProducts() {
        val saleItems = mutableListOf<SaleItem>()
        val totalProducts = productsMap.size
        if (totalProducts == 0) {
            productsRecyclerView.adapter = SaleDetailAdapter(saleItems)
            return
        }
        var loaded = 0
        productsMap.forEach { (id, qty) ->
            mDBRef.child("Inventario").child(id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(Product::class.java)?.let { product ->
                            val itemTotal = product.price * qty
                            saleItems.add(SaleItem(product.name, itemTotal, qty))
                        }
                        loaded++
                        if (loaded == totalProducts) {
                            productsRecyclerView.adapter = SaleDetailAdapter(saleItems)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        loaded++
                        if (loaded == totalProducts) {
                            productsRecyclerView.adapter = SaleDetailAdapter(saleItems)
                        }
                    }
                })
        }
    }

    private data class SaleItem(
        val name: String,
        val totalPrice: Double,
        val quantity: Int
    )

    private inner class SaleDetailAdapter(
        private val items: List<SaleItem>
    ) : RecyclerView.Adapter<SaleDetailAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_exact, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.productName    .text = item.name
            holder.productPrice   .text = "%,.2f COP".format(item.totalPrice)
            holder.productQuantity.text = item.quantity.toString()
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val productName    : TextView = itemView.findViewById(R.id.productName)
            val productPrice   : TextView = itemView.findViewById(R.id.productPrice)
            val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        }
    }
}
