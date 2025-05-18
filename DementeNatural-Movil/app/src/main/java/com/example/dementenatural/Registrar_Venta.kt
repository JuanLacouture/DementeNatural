package com.example.dementenatural

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
    private lateinit var confirmSaleButton: CardView

    private var total: Double = 0.0
    private val selectedProducts = mutableMapOf<String, Int>()
    private var userSede: String? = null

    private lateinit var productAdapter: ProductAdapter
    private lateinit var summaryAdapter: SummaryAdapter
    private val summaryItems = mutableListOf<SummaryItem>()

    private var userStartedTyping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_venta)

        // --- VINCULACIÓN DE VISTAS ---
        searchInput        = findViewById(R.id.searchInput)
        productList        = findViewById(R.id.productList)
        summaryList        = findViewById(R.id.summaryList)
        totalAmount        = findViewById(R.id.totalAmount)
        confirmSaleButton  = findViewById(R.id.confirmSaleButton)

        // --- ADAPTERS ---
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

        // --- AL TOCAR EL EDIT TEXT: mostrar TODO la primera vez ---
        searchInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && userSede != null && !userStartedTyping) {
                userStartedTyping = true
                searchProducts("", userSede!!)
            } else if (!hasFocus && searchInput.text.isEmpty()) {
                productAdapter.updateProducts(emptyList())
                userStartedTyping = false
            }
        }

        // --- FILTRO EN CADA TECLA ---
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (userSede == null) return
                searchProducts(query, userSede!!)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- CONFIRMAR VENTA ---
        confirmSaleButton.setOnClickListener {
            confirmSale()
        }

        loadUserSede()
    }

    /**
     * Interceptamos todos los TOUCH_EVENTS para detectar toques fuera
     * del EditText y del RecyclerView de búsqueda.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                // Rectángulo del EditText
                val outRectEdit = Rect()
                v.getGlobalVisibleRect(outRectEdit)
                // Rectángulo del RecyclerView de resultados
                val outRectList = Rect()
                productList.getGlobalVisibleRect(outRectList)

                // Si el tap NO cae en ninguno de los dos, cerramos la búsqueda:
                if (!outRectEdit.contains(ev.rawX.toInt(), ev.rawY.toInt()) &&
                    !outRectList.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                    productAdapter.updateProducts(emptyList())
                    userStartedTyping = false
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /** Oculta el teclado físico al usuario. */
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadUserSede() {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("email").equalTo(user?.email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userSede = snapshot.children
                        .firstOrNull()
                        ?.getValue(User::class.java)
                        ?.sede
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Registrar_Venta,
                        "Error al cargar sede",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun searchProducts(query: String, sede: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Inventario")
        ref.orderByChild("sede").equalTo(sede)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filtered = mutableListOf<Product>()
                    for (snap in snapshot.children) {
                        val p = snap.getValue(Product::class.java) ?: continue
                        if (query.isEmpty() || p.name.startsWith(query, true)) {
                            filtered.add(p.copy(id = snap.key ?: ""))
                        }
                    }
                    productAdapter.updateProducts(filtered)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Registrar_Venta,
                        "Error en búsqueda",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateSummary(prodId: String, qty: Int) {
        val idx = summaryItems.indexOfFirst { it.id == prodId }
        val prod = productAdapter.currentProducts.firstOrNull { it.id == prodId }
        if (qty == 0) {
            if (idx >= 0) {
                summaryItems.removeAt(idx)
                summaryAdapter.notifyItemRemoved(idx)
            }
            selectedProducts.remove(prodId)
        } else {
            if (prod == null) return
            val item = SummaryItem(prodId, prod.name, qty, prod.price * qty)
            if (idx >= 0) {
                summaryItems[idx] = item
                summaryAdapter.notifyItemChanged(idx)
            } else {
                summaryItems.add(item)
                summaryAdapter.notifyItemInserted(summaryItems.size - 1)
            }
            selectedProducts[prodId] = qty
        }
    }

    private fun calculateTotal() {
        total = summaryItems.sumOf { it.subtotal }
        totalAmount.text = "%,.2f COP".format(total)
    }

    private fun confirmSale() {
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Agrega productos primero", Toast.LENGTH_SHORT).show()
            return
        }
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
            ?: return Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()

        val ventasRef = FirebaseDatabase.getInstance().getReference("Ventas")
        val saleId   = ventasRef.push().key
            ?: return Toast.makeText(this, "Error al generar ID de venta", Toast.LENGTH_SHORT).show()

        val inventarioRef = FirebaseDatabase.getInstance().getReference("Inventario")
        inventarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = hashMapOf<String, Any>()
                val errores = mutableListOf<String>()

                selectedProducts.forEach { (id, qty) ->
                    val prod = snapshot.child(id).getValue(Product::class.java)
                    when {
                        prod == null                -> errores.add("Producto no encontrado: $id")
                        prod.quantity < qty         -> errores.add("${prod.name} (Stock: ${prod.quantity})")
                        else                        -> updates["$id/quantity"] = prod.quantity - qty
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

                val sale = Sale(saleId, userEmail, selectedProducts.toMap(), total)
                ventasRef.child(saleId).setValue(sale)
                    .addOnSuccessListener {
                        inventarioRef.updateChildren(updates)
                            .addOnSuccessListener {
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
