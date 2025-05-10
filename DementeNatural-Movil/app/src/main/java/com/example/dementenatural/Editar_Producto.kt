package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Editar_Producto : AppCompatActivity() {

    private lateinit var mDBRef: DatabaseReference

    private lateinit var productId: String
    private lateinit var editProductName: EditText
    private lateinit var editProductDescription: EditText
    private lateinit var editProductPrice: EditText
    private lateinit var editProductQuantity: EditText

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var productSede: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_producto)

        mDBRef = FirebaseDatabase.getInstance().reference

        // Obtener los datos del producto a editar
        productId = intent.getStringExtra("product_id") ?: ""
        val productName = intent.getStringExtra("product_name") ?: ""
        val productDescription = intent.getStringExtra("product_description") ?: ""
        val productPrice = intent.getDoubleExtra("product_price", 0.0)
        val productQuantity = intent.getIntExtra("product_quantity", 0)
        productSede = intent.getStringExtra("product_sede") ?: ""

        // Referencias a los campos de edición
        editProductName = findViewById(R.id.inputProductName)
        editProductDescription = findViewById(R.id.inputDescription)
        editProductPrice = findViewById(R.id.inputPrice)
        editProductQuantity = findViewById(R.id.inputQuantity)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)

        // Cargar datos actuales del producto
        editProductName.setText(productName)
        editProductDescription.setText(productDescription)
        editProductPrice.setText(productPrice.toString())
        editProductQuantity.setText(productQuantity.toString())

        // Deshabilitar la sede, ya que no puede ser modificada
        // Aquí se puede agregar un campo si deseas mostrar la sede, pero no permitir editarlo
        // Por ejemplo:
        // editProductSede.setText(productSede)
        // editProductSede.isEnabled = false // Si quieres que se vea, pero no se edite

        // Guardar los cambios
        saveButton.setOnClickListener {
            val updatedName = editProductName.text.toString()
            val updatedDescription = editProductDescription.text.toString()
            val updatedPrice = editProductPrice.text.toString().toDouble()
            val updatedQuantity = editProductQuantity.text.toString().toInt()

            // Validar que los campos no estén vacíos
            if (updatedName.isBlank() || updatedDescription.isBlank() || updatedPrice <= 0 || updatedQuantity <= 0) {
                Toast.makeText(this, "Todos los campos son obligatorios y el precio y cantidad deben ser mayores a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Actualizar producto en Firebase, la sede no se cambia
            val updatedProduct = Product(
                id = productId,
                name = updatedName,
                description = updatedDescription,
                price = updatedPrice,
                quantity = updatedQuantity,
                sede = productSede // La sede se mantiene igual
            )

            mDBRef.child("Inventario").child(productId).setValue(updatedProduct)
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()

                    // Regresar al Menu_Admin después de la actualización
                    val intent = Intent(this, Menu_Admin::class.java)
                    startActivity(intent)
                    finish() // Finalizamos la actividad actual para no quedarnos en Editar_Producto
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el producto: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Cancelar acción y regresar a la pantalla anterior
        cancelButton.setOnClickListener {
            finish()
        }
    }
}
