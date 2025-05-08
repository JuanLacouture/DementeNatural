package com.example.dementenatural

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registro : AppCompatActivity() {

    private lateinit var spinnerRol: Spinner
    private lateinit var spinnerSede: Spinner
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonRegistrar: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        // Ajuste de insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().reference

        // Referencias UI
        spinnerRol      = findViewById(R.id.spinnerRol)
        spinnerSede     = findViewById(R.id.spinnerSede)
        editEmail       = findViewById(R.id.editEmail)
        editPassword    = findViewById(R.id.editPassword)
        buttonRegistrar = findViewById(R.id.buttonRegistrar)

        // Configurar spinners con hint y valores personalizados
        setupHintSpinner(
            spinnerRol,
            "Selecciona rol",
            arrayOf("Administrador", "Trabajador")
        )
        setupHintSpinner(
            spinnerSede,
            "Selecciona sede",
            arrayOf("Sede1", "Sede2", "Sede3")
        )

        // Al pulsar Registrar
        buttonRegistrar.setOnClickListener {
            val email    = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val rolPos   = spinnerRol.selectedItemPosition
            val sedePos  = spinnerSede.selectedItemPosition

            // Validaciones
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email y contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (rolPos == 0 || sedePos == 0) {
                Toast.makeText(this, "Por favor selecciona rol y sede", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol  = spinnerRol.selectedItem as String
            val sede = spinnerSede.selectedItem as String

            registerUser(email, password, rol, sede)
        }
    }

    private fun registerUser(email: String, password: String, rol: String, sede: String) {
        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuario creado, ahora guardamos datos adicionales en Realtime DB
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(uid, email, rol, sede)
                    mDBRef.child("Users").child(uid).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            // Aquí podrías navegar a otra Activity, p.ej. Login o Main
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setupHintSpinner(spinner: Spinner, hintText: String, items: Array<String>) {
        val allItems = mutableListOf<String>()
        allItems.add(hintText)
        allItems.addAll(items)

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allItems) {
            override fun isEnabled(position: Int) = position != 0
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(
                    if (position == 0)
                        Color.GRAY
                    else
                        resources.getColor(R.color.dark_text, theme)
                )
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0, false)
    }
}

// Modelo de datos para usuario
data class User(
    val uid: String = "",
    val email: String = "",
    val rol: String = "",
    val sede: String = ""
)
