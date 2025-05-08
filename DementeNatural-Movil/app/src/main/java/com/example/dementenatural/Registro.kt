package com.example.dementenatural

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registro : AppCompatActivity() {

    private lateinit var spinnerRol: Spinner
    private lateinit var spinnerSede: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        spinnerRol = findViewById(R.id.spinnerRol)
        spinnerSede = findViewById(R.id.spinnerSede)

        // Configurar spinner de Rol
        setupHintSpinner(
            spinnerRol,
            "Rol",
            arrayOf("Administrador", "Usuario", "Invitado")
        )

        // Configurar spinner de Sede
        setupHintSpinner(
            spinnerSede,
            "Sede",
            arrayOf("Lima", "Arequipa", "Trujillo")
        )

        // Aquí puedes agregar el resto de tu inicialización, como listeners para el botón de registro, etc.
    }

    private fun setupHintSpinner(spinner: Spinner, hintText: String, items: Array<String>) {
        // Crear la lista con el hint como primer elemento
        val allItems = mutableListOf<String>()
        allItems.add(hintText)  // El primer elemento es el hint
        allItems.addAll(items)

        // Crear adaptador personalizado
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            allItems
        ) {
            override fun isEnabled(position: Int): Boolean {
                // El primer elemento (hint) no debe ser seleccionable
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView

                // Estilizar el hint en el dropdown
                if (position == 0) {
                    textView.setTextColor(Color.GRAY)
                } else {
                    textView.setTextColor(Color.BLACK)
                }
                return view
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView

                // Estilizar el texto seleccionado
                if (position == 0) {
                    // El hint debe verse como un placeholder
                    textView.setTextColor(Color.GRAY)
                } else {
                    // Una selección real debe verse destacada
                    textView.setTextColor(resources.getColor(R.color.dark_text, theme))
                }

                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Establecer el hint como selección inicial
        spinner.setSelection(0, false)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    val textView = it as TextView
                    if (position > 0) {
                        textView.setTextColor(resources.getColor(R.color.dark_text, theme))
                    } else {
                        textView.setTextColor(Color.GRAY)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No es necesario hacer nada aquí
            }
        }
    }

    // Método para verificar si se seleccionó un elemento real (no el hint)
    private fun isValidSelection(spinner: Spinner): Boolean {
        return spinner.selectedItemPosition > 0
    }
}