package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Menu_Trabajador : AppCompatActivity() {

    private lateinit var cardRegistrarVenta: CardView
    private lateinit var cardWhatsapp: CardView
    private lateinit var cardConsultarInventario: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_trabajador)

        // Ajuste de insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Referencias a los CardViews
        cardRegistrarVenta       = findViewById(R.id.cardRegistrarVenta)
        cardWhatsapp             = findViewById(R.id.cardWhatsapp)
        cardConsultarInventario  = findViewById(R.id.cardConsultarInventario)

        // Al hacer click, navegamos según el botón
        cardRegistrarVenta.setOnClickListener {
            // Navegar a la pantalla de registro de venta
            startActivity(Intent(this, Registrar_Venta::class.java))
        }

        cardWhatsapp.setOnClickListener {
            // Navegar a la pantalla de generación de enlace de WhatsApp
            startActivity(Intent(this, Generar_Enlace::class.java))
        }

        cardConsultarInventario.setOnClickListener {
            // Navegar a la pantalla de consulta de inventario
            startActivity(Intent(this, Consultar_Inventario::class.java))
        }
    }
}
