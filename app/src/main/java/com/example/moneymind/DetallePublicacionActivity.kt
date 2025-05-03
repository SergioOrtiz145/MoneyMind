package com.example.moneymind

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.BoringLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymind.databinding.ActivityDetallePublicacionBinding
import com.example.moneymind.model.Comentario
import com.example.moneymind.model.ComentarioAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetallePublicacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePublicacionBinding
    private lateinit var comentariosRef: DatabaseReference
    private val listaComentarios = mutableListOf<Comentario>()
    private lateinit var adapter: ComentarioAdapter
    private lateinit var publicacionId: String
    private var primeraVez: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        publicacionId = intent.getStringExtra("publicacionId") ?: return
        val publicacionRef = FirebaseDatabase.getInstance().getReference("foro/publicaciones/$publicacionId")
        comentariosRef = publicacionRef.child("comentarios")

        // Configurar RecyclerView de comentarios
        adapter = ComentarioAdapter(listaComentarios)
        binding.recyclerComentarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerComentarios.adapter = adapter

        // Cargar datos de la publicación
        publicacionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.textTitulo.text = snapshot.child("titulo").getValue(String::class.java)
                binding.textContenido.text = snapshot.child("contenido").getValue(String::class.java)
                binding.textAutor.text = snapshot.child("autor").getValue(String::class.java)

                val fechaLong = snapshot.child("fecha").getValue(Long::class.java) ?: 0
                binding.textFecha.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                    Date(fechaLong)
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetallePublicacionActivity, "Error al cargar publicación", Toast.LENGTH_SHORT).show()
            }
        })

        // Cargar comentarios
        cargarComentarios()

        // Enviar comentario
        binding.btnComentar.setOnClickListener {
            val texto = binding.editComentario.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarComentario(texto)
                binding.editComentario.setText("")
            }
        }
        binding.atrasButton.setOnClickListener {
            finish()
        }
    }

    private fun cargarComentarios() {
        comentariosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaComentarios.clear()
                for (comSnap in snapshot.children) {
                    val comentario = comSnap.getValue(Comentario::class.java)
                    comentario?.let { listaComentarios.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetallePublicacionActivity, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enviarComentario(texto: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("name").getValue(String::class.java) ?: ""
                val apellido = snapshot.child("lastName").getValue(String::class.java) ?: ""
                val autor = "$nombre $apellido".trim()

                val comentarioId = comentariosRef.push().key ?: return
                val comentario = Comentario(
                    id = comentarioId,
                    texto = texto,
                    autor = autor,
                    fecha = System.currentTimeMillis()
                )

                comentariosRef.child(comentarioId).setValue(comentario)
                    .addOnSuccessListener {
                        if(primeraVez){
                            //mostrar logro
                            primeraVez = false
                            val intent = Intent(baseContext, LogroActivity::class.java)
                            intent.putExtra("titulo", "Primer comentario")
                            startActivity(intent)
                            setResult(Activity.RESULT_OK)
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
