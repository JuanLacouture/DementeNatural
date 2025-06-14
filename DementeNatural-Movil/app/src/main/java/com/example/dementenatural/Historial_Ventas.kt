package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Historial_Ventas : AppCompatActivity() {

    private lateinit var mDBRef: DatabaseReference
    private lateinit var recycler: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var statsButton: CardView

    private val ventasList = mutableListOf<Sale>()
    private val allVentas    = mutableListOf<Sale>()
    private lateinit var ventasAdapter: VentasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial_ventas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        mDBRef = FirebaseDatabase.getInstance().reference

        // RecyclerView & Adapter
        recycler = findViewById(R.id.salesList)
        recycler.layoutManager = LinearLayoutManager(this)
        ventasAdapter = VentasAdapter(ventasList) { sale ->
            val intent = Intent(this, Venta_Especifica::class.java).apply {
                putExtra("saleId", sale.saleId)
                putExtra("email", sale.email)
                putExtra("total", sale.total)
                putExtra("products", HashMap(sale.products))  // Map<String,Int> es Serializable
            }
            startActivity(intent)
        }
        recycler.adapter = ventasAdapter

        // Search Input
        searchInput = findViewById(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterVentas(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Statistics Button
        statsButton = findViewById(R.id.statisticsButton)
        statsButton.setOnClickListener {
            startActivity(Intent(this, Estadisticas_Ventas::class.java))
        }

        // Load data
        cargarVentas()
    }

    private fun cargarVentas() {
        mDBRef.child("Ventas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ventasList.clear()
                allVentas.clear()
                for (child in snapshot.children) {
                    child.getValue(Sale::class.java)?.let {
                        ventasList.add(it)
                        allVentas.add(it)
                    }
                }
                ventasAdapter.notifyDataSetChanged()
                if (ventasList.isEmpty()) {
                    Toast.makeText(this@Historial_Ventas, "No hay ventas registradas", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Historial_Ventas, "Error al cargar ventas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterVentas(query: String) {
        ventasList.clear()
        if (query.isEmpty()) {
            ventasList.addAll(allVentas)
        } else {
            ventasList.addAll(allVentas.filter {
                it.saleId.contains(query, ignoreCase = true)
            })
        }
        ventasAdapter.notifyDataSetChanged()
    }
}
