package com.example.moneymind

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymind.databinding.FragmentForoBinding
import com.example.moneymind.model.Publicacion
import com.example.moneymind.model.PublicacionAdapter
import com.google.firebase.database.*

class ForoFragment : Fragment() {
    private var _binding: FragmentForoBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private val listaPublicaciones = mutableListOf<Publicacion>()
    private lateinit var adapter: PublicacionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForoBinding.inflate(inflater, container, false)

        // Configurar RecyclerView
        adapter = PublicacionAdapter(listaPublicaciones) { publicacion ->
            val intent = Intent(requireContext(), DetallePublicacionActivity::class.java)
            intent.putExtra("publicacionId", publicacion.id)
            startActivity(intent)
        }
        binding.recyclerPublicaciones.adapter = adapter
        binding.recyclerPublicaciones.layoutManager = LinearLayoutManager(context)

        // Inicializar referencia a Firebase
        database = FirebaseDatabase.getInstance().getReference("foro/publicaciones")

        // Cargar publicaciones
        cargarPublicaciones()

        // Botón para crear nueva publicación
        binding.btnCrearPublicacion.setOnClickListener {
            val intent = Intent(requireContext(), CrearPublicacionActivity::class.java)
            startActivityForResult(intent, 100) // para recargar al volver
        }


        return binding.root
    }

    // Cargar solo las 10 publicaciones más recientes, una sola vez
    private fun cargarPublicaciones() {
        database.orderByChild("fecha").limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaPublicaciones.clear()
                    for (pubSnap in snapshot.children) {
                        val pub = pubSnap.getValue(Publicacion::class.java)
                        pub?.let { listaPublicaciones.add(it) }
                    }
                    listaPublicaciones.sortByDescending { it.fecha }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error al cargar publicaciones", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Recargar publicaciones al volver del formulario de nueva publicación
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            cargarPublicaciones()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
