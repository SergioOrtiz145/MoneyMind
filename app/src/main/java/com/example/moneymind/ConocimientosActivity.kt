package com.example.moneymind

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymind.databinding.ActivityConocimientosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ConocimientosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConocimientosBinding
    private lateinit var auth: FirebaseAuth
    private val botonSeleccionado = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConocimientosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val list = listOf(binding.button1, binding.button2, binding.button3, binding.button4, binding.button5)
        val colorSeleccionado = Color.parseColor("#4CAF50") // Verde
        val colorDefault = Color.WHITE

        for (boton in list) {
            // Inicializamos el estado
            boton.tag = false

            boton.setOnClickListener {
                val seleccionado = boton.tag as Boolean

                if (!seleccionado) {
                    boton.setBackgroundColor(colorSeleccionado)
                    boton.tag = true
                    botonSeleccionado[boton.text.toString()] = true
                } else {
                    boton.setBackgroundColor(colorDefault)
                    boton.tag = false
                    botonSeleccionado[boton.text.toString()] = false
                }
            }
        }
        binding.continuarButton.setOnClickListener {
            guardarSeleccionEnFirebase()
            startActivity(Intent(this, TestActivity::class.java))
        }

    }
    private fun guardarSeleccionEnFirebase() {
        val user = auth.currentUser

        if (user != null) {
            val database = FirebaseDatabase.getInstance()
            val ref = database.reference.child("users").child(user.uid).child("conocimientos")

            // Filtrar solo los botones que están seleccionados
            val conocimientosSeleccionados = botonSeleccionado.filter { it.value }.keys.toList()

            ref.setValue(conocimientosSeleccionados)
                .addOnSuccessListener {
                    // Guardado exitoso
                }
                .addOnFailureListener { e ->
                    // Manejar error
                    e.printStackTrace()
                }
        }
    }
}
