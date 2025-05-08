package com.example.dementenatural

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Ajuste de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Firebase
        auth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().reference

        // Referencias UI
        editEmail    = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        buttonLogin  = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val email    = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // Validaciones básicas
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
            if (password.contains(" ")) {
                Toast.makeText(this, "La contraseña no puede contener espacios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Intentar login en Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Obtener UID del usuario
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        // Leer rol desde Realtime Database
                        mDBRef.child("Users").child(uid).child("rol")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val rol = snapshot.getValue(String::class.java)
                                    if (rol == "Administrador") {
                                        startActivity(Intent(this@login, Menu_Admin::class.java))
                                    } else {
                                        startActivity(Intent(this@login, Menu_Trabajador::class.java))
                                    }
                                    finish()
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(
                                        this@login,
                                        "Error al leer rol: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                    } else {
                        Toast.makeText(
                            this,
                            "Error de autenticación: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
