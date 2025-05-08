// Menu_Trabajador.kt
package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Menu_Trabajador : AppCompatActivity() {

    private lateinit var welcomeMessage: TextView
    private lateinit var cardRegistrarVenta: CardView
    private lateinit var cardWhatsapp: CardView
    private lateinit var cardConsultarInventario: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_trabajador)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: "usuario"

        // Referencias UI
        welcomeMessage         = findViewById(R.id.welcomeMessage)
        cardRegistrarVenta     = findViewById(R.id.cardRegistrarVenta)
        cardWhatsapp           = findViewById(R.id.cardWhatsapp)
        cardConsultarInventario= findViewById(R.id.cardConsultarInventario)

        // Mostrar correo en bienvenida
        welcomeMessage.text = "Bienvenido, $email"

        // Navegación
        cardRegistrarVenta.setOnClickListener {
            startActivity(Intent(this, Registrar_Venta::class.java))
        }
        cardWhatsapp.setOnClickListener {
            startActivity(Intent(this, Generar_Enlace::class.java))
        }
        cardConsultarInventario.setOnClickListener {
            startActivity(Intent(this, Consultar_Inventario::class.java))
        }
    }
}
