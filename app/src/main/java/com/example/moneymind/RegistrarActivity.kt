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
import com.example.moneymind.databinding.ActivityRegistrarBinding
import com.example.moneymind.model.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistrarActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRegistrarBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
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
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")
        binding.buttonSignUp.setOnClickListener {
            val nombre = binding.editTextFirstName.text.toString()
            val apellido = binding.editTextLastName.text.toString()
            val correo = binding.editTextEmail.text.toString()
            val contra = binding.editTextPassword.text.toString()
            val confirmContra = binding.editTextConfirmPassword.text.toString()
            if(nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && contra.isNotEmpty() && confirmContra.isNotEmpty()){
                registrarUsuario(nombre, apellido, correo, contra, confirmContra)
            }else
                Toast.makeText(baseContext, "Ingrese la información en todos los campos", Toast.LENGTH_LONG).show()
        }
    }
    private fun registrarUsuario(nombre: String, apellido: String, correo: String, contra: String, confirmContra: String){
        if (contra != confirmContra) {
            // Los passwords no coinciden
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }


        FirebaseService.auth.createUserWithEmailAndPassword(correo, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = FirebaseService.auth.currentUser
                    val userId = user?.uid

                    // Guarda la información adicional en Firebase Database
                    val database = FirebaseDatabase.getInstance().reference
                    val userRef = database.child("users").child(userId!!)
                    //logros
                    var primerModulo = false
                    var modulos3 = false
                    var completarNivel = false
                    var primeraPublicacion = false
                    var publicaciones5 = false
                    var primerComentario = false
                    var comentarios5 = false

                    val userData = mapOf(
                        "name" to nombre,
                        "lastName" to apellido,
                        "email" to correo,
                        "primerModulo" to primerModulo,
                        "modulos3" to modulos3,
                        "completarNivel" to completarNivel,
                        "primeraPublicacion" to primeraPublicacion,
                        "publicaciones5" to publicaciones5,
                        "primerComentario" to primerComentario,
                        "comentarios5" to comentarios5
                    )

                    userRef.setValue(userData).addOnCompleteListener { databaseTask ->
                        if (databaseTask.isSuccessful) {
                            Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                            clearSharedPreferences()
                            startActivity(Intent(this, ConocimientosActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error al guardar los datos en la base de datos", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    // Si el registro falla, muestra el mensaje de error
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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