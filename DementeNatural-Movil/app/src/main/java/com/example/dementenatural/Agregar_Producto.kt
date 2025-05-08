package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Agregar_Producto : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var inputProductName: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputPrice: EditText
    private lateinit var inputQuantity: EditText
    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_producto)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Firebase
        auth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().reference

        // UI referencias
        backButton         = findViewById(R.id.backButton)
        inputProductName   = findViewById(R.id.inputProductName)
        inputDescription   = findViewById(R.id.inputDescription)
        inputPrice         = findViewById(R.id.inputPrice)
        inputQuantity      = findViewById(R.id.inputQuantity)
        buttonCancel       = findViewById(R.id.buttonCancel)
        buttonSave         = findViewById(R.id.buttonSave)

        // 1) Flecha “back” vuelve a Gestionar_Inventario
        backButton.setOnClickListener {
            finish()
        }
        buttonCancel.setOnClickListener {
            finish()
        }

        // 2) Guardar producto
        buttonSave.setOnClickListener {
            val name = inputProductName.text.toString().trim()
            val desc = inputDescription.text.toString().trim()
            val priceText = inputPrice.text.toString().trim()
            val qtyText = inputQuantity.text.toString().trim()

            // 3a) Nombre: >5 caracteres, solo letras y números
            if (name.length < 6) {
                Toast.makeText(this, "El nombre debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!name.matches("^[\\p{L}0-9 ]+$".toRegex())) {
                Toast.makeText(this, "El nombre no puede contener caracteres especiales", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3b) Descripción: opcional, máximo 30 palabras
            if (desc.isNotEmpty()) {
                val words = desc.split("\\s+".toRegex())
                if (words.size > 30) {
                    Toast.makeText(this, "La descripción no puede superar 30 palabras", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // 3c) Precio: >= 0.1, no negativos
            val price = priceText.toDoubleOrNull()
            if (price == null || price < 0.1) {
                Toast.makeText(this, "El precio debe ser al menos 0.1 y sin negativos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3d) Cantidad: >= 1, no negativos
            val qty = qtyText.toIntOrNull()
            if (qty == null || qty < 1) {
                Toast.makeText(this, "La cantidad debe ser al menos 1 y sin negativos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener sede del usuario activo
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mDBRef.child("Users").child(uid).child("sede")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val sede = snapshot.getValue(String::class.java) ?: ""

                        // Crear objeto producto
                        val product = Product(
                            name = name,
                            description = desc,
                            price = price,
                            quantity = qty,
                            sede = sede
                        )

                        // Push a "Inventario"
                        mDBRef.child("Inventario").push()
                            .setValue(product)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@Agregar_Producto,
                                    "Producto agregado exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Volver a Gestionar_Inventario
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@Agregar_Producto,
                                    "Error al guardar: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@Agregar_Producto,
                            "Error al leer sede: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}

// Modelo de datos Producto
data class Product(
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val sede: String = ""
)
