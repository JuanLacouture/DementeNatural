package com.example.dementenatural

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Registrar_Venta : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var productList: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var confirmSaleButton: View
    private var total: Double = 0.0
    private val selectedProducts = mutableMapOf<String, Int>() // ID del producto -> Cantidad
    private var userSede: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_venta)

        searchInput = findViewById(R.id.searchInput)
        productList = findViewById(R.id.productList)
        totalAmount = findViewById(R.id.totalAmount)
        confirmSaleButton = findViewById(R.id.confirmSaleButton)

        // Configurar RecyclerView inicialmente vacío
        productList.layoutManager = LinearLayoutManager(this)
        productList.adapter = ProductAdapter(emptyList()) { _, _ -> }

        // Inicializar total en 0
        totalAmount.text = "0 COP"

        // Listener para búsqueda (corregido)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (query.isNotEmpty() && userSede != null) {
                    searchProducts(query, userSede!!)
                } else {
                    (productList.adapter as ProductAdapter).updateProducts(emptyList())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmSaleButton.setOnClickListener {
            confirmSale()
        }

        loadUserSede()
    }

    private fun loadUserSede() {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("email").equalTo(user?.email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userSede = snapshot.children.firstOrNull()?.getValue(User::class.java)?.sede
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Registrar_Venta, "Error al cargar sede", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun searchProducts(query: String, sede: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Inventario")
        ref.orderByChild("sede").equalTo(sede).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredProducts = mutableListOf<Product>()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(Product::class.java)
                    if (product != null && product.name.startsWith(query, true)) {
                        filteredProducts.add(product.copy(id = productSnap.key ?: ""))
                    }
                }
                (productList.adapter as ProductAdapter).updateProducts(filteredProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Registrar_Venta, "Error en búsqueda", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateTotal() {
        total = 0.0
        selectedProducts.forEach { (id, qty) ->
            (productList.adapter as ProductAdapter).currentProducts.firstOrNull { it.id == id }?.let {
                total += it.price * qty
            }
        }
        totalAmount.text = "%,.2f COP".format(total)
    }

    private fun confirmSale() {
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Agrega productos primero", Toast.LENGTH_SHORT).show()
            return
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val ventasRef = FirebaseDatabase.getInstance().getReference("Ventas")
        val saleId = ventasRef.push().key
        val inventarioRef = FirebaseDatabase.getInstance().getReference("Inventario")

        inventarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = hashMapOf<String, Any>()
                val errores = mutableListOf<String>()

                // Verificar stock
                selectedProducts.forEach { (id, qty) ->
                    val product = snapshot.child(id).getValue(Product::class.java)
                    when {
                        product == null -> errores.add("Producto no encontrado: $id")
                        product.quantity < qty -> errores.add("${product.name} (Stock: ${product.quantity})")
                        else -> updates["$id/quantity"] = product.quantity - qty
                    }
                }

                if (errores.isNotEmpty()) {
                    Toast.makeText(
                        this@Registrar_Venta,
                        "Stock insuficiente:\n${errores.joinToString("\n")}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                // Crear y guardar venta
                val sale = Sale(
                    saleId = saleId,
                    email = userEmail,
                    products = selectedProducts.toMap(),
                    total = total
                )

                ventasRef.child(saleId!!).setValue(sale)
                    .addOnSuccessListener {
                        inventarioRef.updateChildren(updates).addOnSuccessListener {
                            selectedProducts.clear()
                            calculateTotal()
                            Toast.makeText(
                                this@Registrar_Venta,
                                "Venta registrada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@Registrar_Venta,
                            "Error al guardar la venta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Registrar_Venta, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Adapter mejorado
    inner class ProductAdapter(
        private var products: List<Product>,
        private val onQuantityChange: (String, Int) -> Unit
    ) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

        var currentProducts = emptyList<Product>()
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
                    selectedProducts[product.id]?.let { currentQty ->
                        if (currentQty > 0) {
                            val newQty = currentQty - 1
                            selectedProducts[product.id] = newQty
                            quantity.text = newQty.toString()
                            onQuantityChange(product.id, newQty)
                            calculateTotal()
                        }
                    }
                }

                increment.setOnClickListener {
                    val currentQty = selectedProducts[product.id] ?: 0
                    val newQty = currentQty + 1
                    selectedProducts[product.id] = newQty
                    quantity.text = newQty.toString()
                    onQuantityChange(product.id, newQty)
                    calculateTotal()
                }
            }
        }
    }

    data class Sale(
        val saleId: String? = null,
        val email: String? = null,
        val products: Map<String, Int> = mapOf(),
        val total: Double = 0.0
    )
}


