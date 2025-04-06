package com.example.moneymind

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.moneymind.databinding.ActivityInicioBinding

class InicioActivity : AppCompatActivity() {
    private lateinit var binding:ActivityInicioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Cargar fragmento inicial
        loadFragment(HomeFragment()) // O cualquier fragmento de inicio

        // Manejar navegación inferior
        /*binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_modulos -> ModulosFragment()
                R.id.nav_foro -> ForoFragment()
                R.id.nav_ajustes -> AjustesFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }*/
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}