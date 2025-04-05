package com.example.moneymind

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneymind.databinding.ActivityIniciarSesionBinding

class IniciarSesionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityIniciarSesionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.atrasButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.signUpText.setOnClickListener{
            startActivity(Intent(this, RegistrarActivity::class.java))
        }
    }
}