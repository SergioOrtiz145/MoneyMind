package com.example.moneymind

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneymind.databinding.ActivityNivelFinanzasBinding

class NivelFinanzasActivity : AppCompatActivity() {
    private lateinit var binding:ActivityNivelFinanzasBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNivelFinanzasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val items = listOf("Selecciona una opción ▼","No tengo conocimientos básicos ", "Entiendo conceptos muy generales", "Manejo conceptos intermedios", "Conozco bastantes conceptos intermedios y avanzados")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        binding.spinner.adapter = adapter
        binding.continuarButton.setOnClickListener {
            binding.overlayView.visibility = View.VISIBLE
            binding.loadingLayout.visibility = View.VISIBLE
            binding.continuarButton.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                binding.overlayView.visibility = View.GONE
                binding.loadingLayout.visibility = View.GONE
                binding.continuarButton.isEnabled = true

                startActivity(Intent(this, InicioActivity::class.java))
            }, 3000) // 3 segundos
        }

    }
}