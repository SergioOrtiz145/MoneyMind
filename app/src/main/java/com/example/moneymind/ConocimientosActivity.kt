package com.example.moneymind

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymind.databinding.ActivityConocimientosBinding

class ConocimientosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConocimientosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConocimientosBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                } else {
                    boton.setBackgroundColor(colorDefault)
                    boton.tag = false
                }
            }
        }
        binding.continuarButton.setOnClickListener {
            startActivity(Intent(this, NivelFinanzasActivity::class.java))
        }

    }
}
