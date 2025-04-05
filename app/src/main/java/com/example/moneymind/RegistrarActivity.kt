package com.example.moneymind

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneymind.databinding.ActivityRegistrarBinding

class RegistrarActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRegistrarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.atrasButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.textViewSignIn.setOnClickListener {
            startActivity(Intent(this, IniciarSesionActivity::class.java))
        }
    }
}