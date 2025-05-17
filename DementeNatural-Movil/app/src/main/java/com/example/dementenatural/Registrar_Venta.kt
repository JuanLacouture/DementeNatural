package com.example.dementenatural

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Registrar_Venta : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var productList: RecyclerView
    private lateinit var summaryList: RecyclerView
    private lateinit var totalAmount: TextView
    private lateinit var confirmSaleButton: CardView  // Cambiado a CardView

    private var total: Double = 0.0
    private val selectedProducts = mutableMapOf<String, Int>() // ID producto -> Cantidad
    private var userSede: String? = null

    private lateinit var productAdapter: ProductAdapter
    private lateinit var summaryAdapter: SummaryAdapter
    private val summaryItems = mutableListOf<SummaryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_venta)

        searchInput = findViewById(R.id.searchInput)
        productList = findViewById(R.id.productList)
        summaryList = findViewById(R.id.summaryList)
        totalAmount = findViewById(R.id.totalAmount)
        confirmSaleButton = findViewById(R.id.confirmSaleButton)

        productAdapter = ProductAdapter(emptyList(), selectedProducts) { prodId, newQty ->
            updateSummary(prodId, newQty)
            calculateTotal()
        }
        productList.layoutManager = LinearLayoutManager(this)
        productList.adapter = productAdapter

        summaryAdapter = SummaryAdapter(summaryItems)
        summaryList.layoutManager = LinearLayoutManager(this)
        summaryList.adapter = summaryAdapter

        totalAmount.text = "0 COP"

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (query.isNotEmpty() && userSede != null) {
                    searchProducts(query, userSede!!)
                } else {
                    productAdapter.updateProducts(emptyList())
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
                productAdapter.updateProducts(filteredProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Registrar_Venta, "Error en búsqueda", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateSummary(prodId: String, qty: Int) {
        val index = summaryItems.indexOfFirst { it.id == prodId }
        val prod = productAdapter.currentProducts.firstOrNull { it.id == prodId }
        if (qty == 0) {
            if (index >= 0) {
                summaryItems.removeAt(index)
                summaryAdapter.notifyItemRemoved(index)
            }
            selectedProducts.remove(prodId)
        } else {
            if (prod == null) return
            val item = SummaryItem(prodId, prod.name, qty, prod.price * qty)
            if (index >= 0) {
                summaryItems[index] = item
                summaryAdapter.notifyItemChanged(index)
            } else {
                summaryItems.add(item)
                summaryAdapter.notifyItemInserted(summaryItems.size - 1)
            }
            selectedProducts[prodId] = qty
        }
    }

    private fun calculateTotal() {
        total = 0.0
        summaryItems.forEach {
            total += it.subtotal
        }
        totalAmount.text = "%,.2f COP".format(total)
    }

    private fun confirmSale() {
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Agrega productos primero", Toast.LENGTH_SHORT).show()
            return
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val ventasRef = FirebaseDatabase.getInstance().getReference("Ventas")
        val saleId = ventasRef.push().key
        if (saleId == null) {
            Toast.makeText(this, "Error al generar ID de venta", Toast.LENGTH_SHORT).show()
            return
        }

        val inventarioRef = FirebaseDatabase.getInstance().getReference("Inventario")

        inventarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = hashMapOf<String, Any>()
                val errores = mutableListOf<String>()

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

                val sale = Sale(
                    saleId = saleId,
                    email = userEmail,
                    products = selectedProducts.toMap(),
                    total = total
                )

                ventasRef.child(saleId).setValue(sale)
                    .addOnSuccessListener {
                        inventarioRef.updateChildren(updates).addOnSuccessListener {
                            selectedProducts.clear()
                            summaryItems.clear()
                            summaryAdapter.notifyDataSetChanged()
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
}
