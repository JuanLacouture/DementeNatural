package com.example.dementenatural

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Consultar_Inventario : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    private lateinit var recycler: RecyclerView
    private lateinit var searchInput: EditText

    // Lista que ve el adapter
    private val productList = mutableListOf<Product>()
    // Copia completa para filtrar
    private val allProducts   = mutableListOf<Product>()

    private lateinit var inventarioAdapter: InventarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_consultar_inventario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // --- Instancias Firebase ---
        auth   = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().reference

        // --- RecyclerView & Adapter ---
        recycler = findViewById(R.id.productList)
        recycler.layoutManager = LinearLayoutManager(this)
        inventarioAdapter = InventarioAdapter(productList)
        recycler.adapter = inventarioAdapter

        // --- Search Input ---
        searchInput = findViewById(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                filterProducts(query)
            }
        })

        // --- Arranque: obtener sede y cargar inventario ---
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        obtenerSedeUsuario(uid) { sede ->
            if (sede.isEmpty()) {
                Toast.makeText(this, "No se encontró la sede de tu usuario", Toast.LENGTH_SHORT).show()
            } else {
                cargarProductosPorSede(sede)
            }
        }
    }

    private fun obtenerSedeUsuario(uid: String, callback: (String) -> Unit) {
        mDBRef.child("Users")
            .child(uid)
            .child("sede")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    callback(snap.getValue(String::class.java) ?: "")
                }
                override fun onCancelled(err: DatabaseError) {
                    Toast.makeText(
                        this@Consultar_Inventario,
                        "Error al obtener sede: ${err.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback("")
                }
            })
    }

    private fun cargarProductosPorSede(sede: String) {
        productList.clear()
        allProducts.clear()
        mDBRef.child("Inventario")
            .orderByChild("sede")
            .equalTo(sede)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.getValue(Product::class.java)?.let {
                            productList.add(it)
                        }
                    }
                    // Guardamos la copia completa
                    allProducts.addAll(productList)

                    inventarioAdapter.notifyDataSetChanged()

                    if (productList.isEmpty()) {
                        Toast.makeText(
                            this@Consultar_Inventario,
                            "No hay productos en tu sede ($sede)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Consultar_Inventario,
                        "Error al cargar productos: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** Filtra por nombre y refresca el adapter */
    private fun filterProducts(query: String) {
        productList.clear()
        if (query.isEmpty()) {
            productList.addAll(allProducts)
        } else {
            val filtered = allProducts.filter {
                it.name.contains(query, ignoreCase = true)
            }
            productList.addAll(filtered)
        }
        inventarioAdapter.notifyDataSetChanged()
    }
}
