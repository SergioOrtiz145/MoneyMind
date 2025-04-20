package com.example.moneymind

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneymind.databinding.ActivityIniciarSesionBinding
import com.example.moneymind.model.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class IniciarSesionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityIniciarSesionBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
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
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")
        binding.loginButton.setOnClickListener {
            val correo = binding.editTextUsername.text.toString()
            val contra = binding.editTextPassword.text.toString()
            if(correo.isNotEmpty() && contra.isNotEmpty()){
                autenticarUsuario(correo, contra)
            }else
                Toast.makeText(baseContext, "Ingrese la información en todos los campos", Toast.LENGTH_LONG).show()
        }
    }
    private fun autenticarUsuario(correo: String, contra: String){
        FirebaseService.auth.signInWithEmailAndPassword(correo, contra)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseService.auth.currentUser
                    Log.d("Auth", "Login exitoso: ${user?.email}")
                    // Limpiar SharedPreferences al entrar
                    clearSharedPreferences()
                    // Obtener el nivel del usuario desde Firebase
                    val uid = FirebaseService.auth.currentUser?.uid
                    uid?.let {
                        usersRef.child(uid).get().addOnSuccessListener { snapshot ->
                            val nivelUsuario = snapshot.child("nivel").getValue(String::class.java)
                            // Guardar el nivel en SharedPreferences
                            saveUserLevelToSharedPreferences(nivelUsuario)
                        }
                    }
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                } else {
                    Log.e("Auth", "Error: ${task.exception?.message}")
                }
            }
    }
    // Función para borrar los datos de SharedPreferences
    private fun clearSharedPreferences() {
        val editor = getSharedPreferences("modulos_preferences", Context.MODE_PRIVATE).edit()
        editor.clear()  // Limpiar todos los datos de SharedPreferences
        editor.apply()  // Aplicar los cambios
        Log.d("SharedPreferences", "Datos de SharedPreferences borrados después de iniciar sesión.")
    }
    // Función para guardar el nivel del usuario en SharedPreferences
    private fun saveUserLevelToSharedPreferences(nivelUsuario: String?) {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Guardar el nivel en SharedPreferences
        nivelUsuario?.let {
            editor.putString("user_level", it)
            editor.apply()
            Log.d("SharedPreferences", "Nivel del usuario guardado en SharedPreferences: $it")
        }
    }
}