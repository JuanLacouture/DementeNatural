// Menu_Admin.kt
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

class Menu_Admin : AppCompatActivity() {

    private lateinit var welcomeMessage: TextView
    private lateinit var cardInventory: CardView
    private lateinit var cardSales: CardView
    private lateinit var cardConsult: CardView
    private lateinit var cardWhatsapp: CardView
    private lateinit var registerNewUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_admin)

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
        welcomeMessage    = findViewById(R.id.welcomeMessage)
        cardInventory     = findViewById(R.id.cardInventory)
        cardSales         = findViewById(R.id.cardSales)
        cardConsult       = findViewById(R.id.cardConsult)
        cardWhatsapp      = findViewById(R.id.cardWhatsapp)
        registerNewUser   = findViewById(R.id.registerNewUser)

        // Mostrar correo en bienvenida
        welcomeMessage.text = "Bienvenido, $email"

        // Navegación
        cardInventory.setOnClickListener {
            startActivity(Intent(this, Gestionar_Inventario::class.java))
        }
        cardSales.setOnClickListener {
            startActivity(Intent(this, Historial_Ventas::class.java))
        }
        cardConsult.setOnClickListener {
            startActivity(Intent(this, Consultar_Inventario::class.java))
        }
        cardWhatsapp.setOnClickListener {
            startActivity(Intent(this, Generar_Enlace::class.java))
        }
        registerNewUser.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
    }
}
