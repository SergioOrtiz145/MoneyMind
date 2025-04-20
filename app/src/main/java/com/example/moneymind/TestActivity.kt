package com.example.moneymind

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymind.databinding.ActivityTestBinding
import com.example.moneymind.model.FirebaseService
import com.example.moneymind.model.Pregunta
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding

    private val listaPreguntas = mutableListOf<Pregunta>()
    private val respuestasUsuario = mutableMapOf<Int, String>()
    private var indexActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarPreguntasDesdeFirebase()

        binding.buttonSiguiente.setOnClickListener {
            procesarRespuesta()
        }
    }

    private fun cargarPreguntasDesdeFirebase() {
        val database = Firebase.database
        val preguntasRef = database.getReference("cuestionario_financiero/preguntas")

        preguntasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaPreguntas.clear()
                for (preguntaSnap in snapshot.children) {
                    val pregunta = preguntaSnap.getValue(Pregunta::class.java)
                    pregunta?.let { listaPreguntas.add(it) }
                }

                if (listaPreguntas.isNotEmpty()) {
                    mostrarPreguntaActual()
                } else {
                    Toast.makeText(this@TestActivity, "No se encontraron preguntas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TestActivity, "Error al cargar preguntas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarPreguntaActual() {
        if (indexActual < listaPreguntas.size) {
            val pregunta = listaPreguntas[indexActual]
            binding.textViewPregunta.text = pregunta.texto
            binding.opcionesPregunta.removeAllViews()

            for (opcion in pregunta.opciones) {
                val rb = RadioButton(this)
                rb.text = opcion
                rb.textSize = 20f
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0, 0, 0, 16)
                rb.layoutParams = layoutParams
                binding.opcionesPregunta.addView(rb)
            }
            binding.opcionesPregunta.clearCheck()

            // Actualizar texto de progreso
            binding.textViewProgreso.text = "Pregunta ${indexActual + 1} de ${listaPreguntas.size}"
        } else {
            Toast.makeText(this, "No hay más preguntas disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarRespuesta() {
        val seleccion = binding.opcionesPregunta.checkedRadioButtonId

        if (seleccion != -1) {
            val radioButton = findViewById<RadioButton>(seleccion)
            val respuesta = radioButton?.text.toString() // Asegúrate de que el RadioButton no sea null

            if (respuesta.isNotEmpty()) {
                respuestasUsuario[indexActual] = respuesta
                indexActual++

                if (indexActual < listaPreguntas.size) {
                    if (indexActual == listaPreguntas.size - 1) {
                        binding.buttonSiguiente.text = "Finalizar"
                    }
                    mostrarPreguntaActual()
                } else {
                    evaluarResultados()
                }
            } else {
                Toast.makeText(this, "La opción seleccionada está vacía", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Selecciona una opción", Toast.LENGTH_SHORT).show()
        }
    }

    private fun evaluarResultados() {
        var puntaje = 0
        for ((index, respuesta) in respuestasUsuario) {
            if (respuesta == listaPreguntas[index].respuestaCorrecta) {
                puntaje++
            }
        }

        val nivel = when {
            puntaje >= 6 -> "avanzado"
            puntaje >= 4 -> "intermedio"
            else -> "básico"
        }
        guardarResultado(nivel)

        Toast.makeText(this, "Tu nivel es: $nivel ($puntaje/${listaPreguntas.size})", Toast.LENGTH_LONG).show()
        //saltar al home de la app
        startActivity(Intent(baseContext, InicioActivity::class.java))

        // Aquí podrías guardar el resultado en Firebase si lo deseas
    }
    private fun guardarResultado(nivel: String){
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Guardar el nivel en SharedPreferences
        nivel?.let {
            editor.putString("user_level", it)
            editor.apply()  // Aplicar los cambios
            Log.d("SharedPreferences", "Nivel del usuario guardado en SharedPreferences: $it")
        }
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val userRef = database.getReference("users/$userId") // Ruta para el usuario específico

            // Guarda el puntaje bajo el ID de usuario
            userRef.child("nivel").setValue(nivel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Puntaje guardado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar el puntaje", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
