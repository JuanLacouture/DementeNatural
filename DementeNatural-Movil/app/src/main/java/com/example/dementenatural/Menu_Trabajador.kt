package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Menu_Trabajador : AppCompatActivity() {

    private lateinit var welcomeMessage: TextView
    private lateinit var cardRegistrarVenta: CardView
    private lateinit var cardWhatsapp: CardView
    private lateinit var cardConsultarInventario: CardView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_trabajador)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Firebase
        auth   = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().reference

        // UI refs
        welcomeMessage          = findViewById(R.id.welcomeMessage)
        cardRegistrarVenta      = findViewById(R.id.cardRegistrarVenta)
        cardWhatsapp            = findViewById(R.id.cardWhatsapp)
        cardConsultarInventario = findViewById(R.id.cardConsultarInventario)

        // Centrar el TextView en runtime (asegúrate que en XML tenga width="0dp" con start/end constraints)
        welcomeMessage.apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER_HORIZONTAL
            // Opcional: si tu layout no lo hace, asegúrate de ajustar el LayoutParams:
            (layoutParams as ConstraintLayout.LayoutParams).apply {
                width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            }
        }

        // Mostrar email y texto provisional de sede
        val email = auth.currentUser?.email ?: "usuario"
        welcomeMessage.text = "Bienvenido, $email\nCargando sede..."

        // Leer la sede desde Realtime DB y actualizar
        auth.currentUser?.uid?.let { uid ->
            mDBRef.child("Users")
                .child(uid)
                .child("sede")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val sede = snapshot.getValue(String::class.java) ?: "Sede desconocida"
                        welcomeMessage.text = "Bienvenido, $email\nSede: $sede"
                    }
                    override fun onCancelled(error: DatabaseError) {
                        welcomeMessage.text = "Bienvenido, $email\n(Sede no disponible)"
                    }
                })
        }

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
