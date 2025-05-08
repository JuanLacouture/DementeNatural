package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Gestionar_Inventario : AppCompatActivity() {

    private lateinit var addProductButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestionar_inventario)

        // Ajuste de insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Referencia al botón "Agregar Productos"
        addProductButton = findViewById(R.id.addProductButton)

        // Al hacer clic, navega a la actividad Agregar_Producto
        addProductButton.setOnClickListener {
            startActivity(
                Intent(this, Agregar_Producto::class.java)
            )
        }
    }
}
