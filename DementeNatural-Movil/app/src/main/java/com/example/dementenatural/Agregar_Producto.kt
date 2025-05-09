package com.example.dementenatural

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        backButton = findViewById(R.id.backButton)
        inputProductName = findViewById(R.id.inputProductName)
        inputDescription = findViewById(R.id.inputDescription)
        inputPrice = findViewById(R.id.inputPrice)
        inputQuantity = findViewById(R.id.inputQuantity)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonSave = findViewById(R.id.buttonSave)

        // Listeners
        backButton.setOnClickListener { finish() }
        buttonCancel.setOnClickListener { finish() }

        buttonSave.setOnClickListener {
            validarYCrearProducto()
        }
    }

    private fun validarYCrearProducto() {
        val name = inputProductName.text.toString().trim()
        val desc = inputDescription.text.toString().trim()
        val priceText = inputPrice.text.toString().trim()
        val qtyText = inputQuantity.text.toString().trim()

        // Validaciones
        if (!validarNombre(name)) return
        if (!validarDescripcion(desc)) return
        val price = validarPrecio(priceText) ?: return
        val qty = validarCantidad(qtyText) ?: return

        // Obtener sede del usuario
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        obtenerSedeUsuario(uid) { sede ->
            if (sede.isEmpty()) {
                Toast.makeText(this, "Error: Sede no encontrada", Toast.LENGTH_SHORT).show()
                return@obtenerSedeUsuario
            }

            // Generar ID único y guardar producto
            val productoRef = mDBRef.child("Inventario").push()
            val product = Product(
                id = productoRef.key ?: "", // Asignar ID generado por Firebase
                name = name,
                description = desc,
                price = price,
                quantity = qty,
                sede = sede
            )

            productoRef.setValue(product)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Producto agregado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun validarNombre(name: String): Boolean {
        return when {
            name.length < 6 -> {
                showError("El nombre debe tener al menos 6 caracteres")
                false
            }
            !name.matches("^[\\p{L}0-9 ]+$".toRegex()) -> {
                showError("No se permiten caracteres especiales")
                false
            }
            else -> true
        }
    }

    private fun validarDescripcion(desc: String): Boolean {
        if (desc.isNotEmpty() && desc.split("\\s+".toRegex()).size > 30) {
            showError("Máximo 30 palabras en la descripción")
            return false
        }
        return true
    }

    private fun validarPrecio(priceText: String): Double? {
        return priceText.toDoubleOrNull()?.takeIf { it >= 0.1 } ?: run {
            showError("Precio mínimo: 0.1")
            null
        }
    }

    private fun validarCantidad(qtyText: String): Int? {
        return qtyText.toIntOrNull()?.takeIf { it >= 1 } ?: run {
            showError("Cantidad mínima: 1")
            null
        }
    }

    private fun obtenerSedeUsuario(uid: String, callback: (String) -> Unit) {
        mDBRef.child("Users").child(uid).child("sede")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.getValue(String::class.java) ?: "")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Agregar_Producto,
                        "Error al obtener sede: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback("")
                }
            })
    }

    private fun showError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}

// Modelo actualizado con ID
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val sede: String = ""
)