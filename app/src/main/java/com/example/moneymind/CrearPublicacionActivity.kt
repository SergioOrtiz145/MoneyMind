package com.example.moneymind

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymind.databinding.ActivityCrearPublicacionBinding
import com.example.moneymind.model.Publicacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CrearPublicacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPublicacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPublicar.setOnClickListener {
            val titulo = binding.edtTitulo.text.toString().trim()
            val contenido = binding.edtContenido.text.toString().trim()

            if (titulo.isEmpty() || contenido.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")

            binding.progressBar.visibility = View.VISIBLE

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.child("name").getValue(String::class.java) ?: ""
                    val apellido = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val autor = "$nombre $apellido".trim()

                    val database = FirebaseDatabase.getInstance().getReference("foro/publicaciones")
                    val id = database.push().key ?: return

                    val publicacion = Publicacion(
                        id = id,
                        titulo = titulo,
                        contenido = contenido,
                        autor = autor,
                        fecha = System.currentTimeMillis()
                    )
                    database.child(id).setValue(publicacion).addOnCompleteListener {
                        binding.progressBar.visibility = View.GONE
                        if (it.isSuccessful) {
                            Toast.makeText(this@CrearPublicacionActivity, "Publicación creada", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Log.e("FirebaseError", "Error: ${it.exception?.message}", it.exception)
                            Toast.makeText(this@CrearPublicacionActivity, "Error al publicar", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@CrearPublicacionActivity, "Error al obtener el autor", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Error al obtener el autor: ${error.message}")
                }
            })
        }
    }
}
