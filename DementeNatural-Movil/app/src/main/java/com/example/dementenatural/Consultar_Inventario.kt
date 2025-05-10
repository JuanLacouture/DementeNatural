package com.example.dementenatural

import android.os.Bundle
import android.util.Log
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

    companion object {
        private const val TAG = "DEBUG_ConsultarInv"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var recycler: RecyclerView
    private val productList = mutableListOf<Product>()
    private lateinit var inventarioAdapter: InventarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 🔥")
        enableEdgeToEdge()
        setContentView(R.layout.activity_consultar_inventario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 1) Inicializar Firebase
        auth   = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().reference

        // 2) RecyclerView
        recycler = findViewById(R.id.productList)
        recycler.layoutManager = LinearLayoutManager(this)
        inventarioAdapter = InventarioAdapter(productList)
        recycler.adapter = inventarioAdapter

        // 3) Usuario autenticado?
        val uid = auth.currentUser?.uid
        Log.d(TAG, "UID obtenido: $uid")
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // 4) Obtener sede
        obtenerSedeUsuario(uid) { sede ->
            Log.d(TAG, "Callback obtenerSedeUsuario → '$sede'")
            Toast.makeText(this, "Debug: sede = $sede", Toast.LENGTH_SHORT).show()
            if (sede.isEmpty()) {
                Toast.makeText(this, "No se encontró la sede de tu usuario", Toast.LENGTH_SHORT).show()
            } else {
                // 5) Cargar productos filtrados
                cargarProductosPorSede(sede)
            }
        }
    }

    private fun obtenerSedeUsuario(uid: String, callback: (String) -> Unit) {
        Log.d(TAG, "Leyendo /Users/$uid/sede …")
        mDBRef.child("Users")
            .child(uid)
            .child("sede")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val sede = snap.getValue(String::class.java) ?: ""
                    Log.d(TAG, "onDataChange(sede): '$sede'")
                    callback(sede)
                }
                override fun onCancelled(err: DatabaseError) {
                    Log.e(TAG, "onCancelled(sede): ${err.message}")
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
        Log.d(TAG, "Query Inventario where sede == $sede …")
        productList.clear()
        mDBRef.child("Inventario")
            .orderByChild("sede")
            .equalTo(sede)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Snapshot.childrenCount = ${snapshot.childrenCount}")
                    Toast.makeText(
                        this@Consultar_Inventario,
                        "Debug: encontrados ${snapshot.childrenCount} items",
                        Toast.LENGTH_SHORT
                    ).show()

                    for (child in snapshot.children) {
                        // Ver qué key e hijo trae
                        Log.d(TAG, "  → hijo key=${child.key}, valor=${child.value}")
                        child.getValue(Product::class.java)?.let {
                            productList.add(it)
                        } ?: run {
                            Log.w(TAG, "    ⚠️ ¡No pudo mapearse a Product!")
                        }
                    }
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
                    Log.e(TAG, "onCancelled(consulta): ${error.message}")
                    Toast.makeText(
                        this@Consultar_Inventario,
                        "Error al cargar productos: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
