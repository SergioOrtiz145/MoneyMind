package com.example.moneymind

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.moneymind.databinding.FragmentModulosBinding
import com.example.moneymind.model.FirebaseService
import com.example.moneymind.model.Modulo
import com.example.moneymind.model.ModuloAdapter
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class ModulosFragment : Fragment() {
    private var _binding: FragmentModulosBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var modulosRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModulosBinding.inflate(inflater, container, false)



        // Inicializar la base de datos Firebase
        database = FirebaseDatabase.getInstance()
        modulosRef = database.getReference("modulos/modulos")

        // Verificar si los módulos ya están guardados localmente
        if (isDataSaved()) {
            // Si los módulos ya están guardados, cargar desde SharedPreferences
            val modulosJson = requireActivity().getSharedPreferences("modulos_preferences", Context.MODE_PRIVATE)
                .getString("modulos", null)
            modulosJson?.let {
                Log.d("Modulos", "Cargando módulos desde SharedPreferences...")
                loadModulosFromJson(it)
            }
        } else {
            // Si los módulos no están guardados, cargarlos desde Firebase
            Log.d("Modulos", "Cargando módulos desde Firebase...")
            loadModulosFromFirebase()
        }
        // Cargar el nivel del usuario desde SharedPreferences
        loadUserLevelFromSharedPreferences()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Función para verificar si los datos ya están guardados en SharedPreferences
    private fun isDataSaved(): Boolean {
        val modulosJson = requireActivity().getSharedPreferences("modulos_preferences", Context.MODE_PRIVATE)
            .getString("modulos", null)
        return !modulosJson.isNullOrEmpty()
    }

    // Cargar los módulos desde Firebase en segundo plano usando Coroutines
    private fun loadModulosFromFirebase() {
        // Ejecutar la carga en un hilo de fondo usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val modulosList = mutableListOf<Modulo>()

                // Realizar la consulta a Firebase
                modulosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Iterar sobre los módulos y agregarlos a la lista
                        for (moduloSnapshot in snapshot.children) {
                            val modulo = moduloSnapshot.getValue(Modulo::class.java)
                            modulo?.let {
                                modulosList.add(it)
                            }
                        }

                        // Limpiar SharedPreferences y guardar los nuevos módulos en el hilo principal
                        CoroutineScope(Dispatchers.Main).launch {
                            saveModulosToSharedPreferences(modulosList)
                            showModulos(modulosList) // Mostrar los módulos en la interfaz
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error al cargar los módulos: ${error.message}")
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(requireContext(), "Error al cargar los módulos", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("Firebase", "Error al cargar los módulos: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al cargar los módulos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para guardar los módulos en SharedPreferences
    private fun saveModulosToSharedPreferences(modulos: List<Modulo>) {
        val editor = requireActivity().getSharedPreferences("modulos_preferences", Context.MODE_PRIVATE).edit()

        // Convertir la lista de módulos a JSON usando Gson
        val gson = Gson()
        val modulosJson = gson.toJson(modulos)

        // Guardar el JSON en SharedPreferences
        editor.putString("modulos", modulosJson)
        editor.apply()
        Log.d("SharedPreferences", "Nuevos módulos guardados en SharedPreferences.")
    }

    // Función para cargar los módulos desde el JSON guardado en SharedPreferences
    private fun loadModulosFromJson(modulosJson: String) {
        val gson = Gson()
        val modulosListType = object : TypeToken<List<Modulo>>() {}.type
        val modulosList = gson.fromJson<List<Modulo>>(modulosJson, modulosListType)

        // Mostrar los módulos en la interfaz
        showModulos(modulosList)
    }
    // Cargar el nivel del usuario desde SharedPreferences
    private fun loadUserLevelFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val nivelUsuario = sharedPreferences.getString("user_level", "básico")  // Valor por defecto es "básico"
        Log.d("ModulosFragment", "Nivel del usuario cargado desde SharedPreferences: $nivelUsuario")
        return nivelUsuario
    }

    // Función para mostrar los módulos en los ListViews
    private fun showModulos(modulos: List<Modulo>) {
        val modulosBasicos = modulos.filter { it.nivel == "básico" }
        val modulosIntermedios = modulos.filter { it.nivel == "intermedio" }
        val modulosAvanzados = modulos.filter { it.nivel == "avanzado" }

        // Crear los adaptadores personalizados usando ModuloAdapter
        val adapterBasico = ModuloAdapter(requireContext(), modulosBasicos)
        val adapterIntermedio = ModuloAdapter(requireContext(), modulosIntermedios)
        val adapterAvanzado = ModuloAdapter(requireContext(), modulosAvanzados)

        // Asignar los adaptadores a los ListViews
        binding.listViewBasico.adapter = adapterBasico
        binding.listViewIntermedio.adapter = adapterIntermedio
        binding.listViewAvanzado.adapter = adapterAvanzado
        // Bloquear los módulos que no sean accesibles según el nivel del usuario
                    // Aquí accedemos al nivel del usuario
                    val nivelUsuario = loadUserLevelFromSharedPreferences()
                    Log.d("Nivel Usuario", "El nivel del usuario es: $nivelUsuario")

                    // Bloquear los módulos intermedios si el nivel no es "intermedio"
                    if (nivelUsuario != "intermedio") {
                        binding.listViewIntermedio.isEnabled = false
                        binding.listViewIntermedio.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                        binding.tituloIntermedio.setBackgroundColor(Color.parseColor("#555555"))
                    }

                    // Bloquear los módulos avanzados si el nivel no es "avanzado"
                    if (nivelUsuario != "avanzado") {
                        binding.listViewAvanzado.isEnabled = false
                        binding.listViewAvanzado.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                        binding.tituloAvanzado.setBackgroundColor(Color.parseColor("#555555"))
                    }


    }
}
