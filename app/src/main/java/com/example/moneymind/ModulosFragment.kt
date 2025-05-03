package com.example.moneymind

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var detalleLauncher: ActivityResultLauncher<Intent>


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
        detalleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                // Aquí recuperas lo que devuelve la actividad
                val resultado = data?.getBooleanExtra("cargar",false)
                if(resultado == true){
                    loadUserLevelFromFirebase {
                        val modulosJson = requireActivity().getSharedPreferences("modulos_preferences", Context.MODE_PRIVATE)
                            .getString("modulos", null)
                        modulosJson?.let {
                            Log.d("Modulos", "Cargando módulos desde SharedPreferences...")
                            loadModulosFromJson(it)
                        }
                    }
                }
            }
        }

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

    private fun loadModulosFromFirebase() {
        val modulosList = mutableListOf<Modulo>()

        modulosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (moduloSnapshot in snapshot.children) {
                    val modulo = moduloSnapshot.getValue(Modulo::class.java)
                    modulo?.let {
                        modulosList.add(it)
                    }
                }

                saveModulosToSharedPreferences(modulosList)
                showModulos(modulosList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al cargar los módulos: ${error.message}")
                Toast.makeText(requireContext(), "Error al cargar los módulos", Toast.LENGTH_SHORT).show()
            }
        })
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
    private fun loadUserLevelFromFirebase(onComplete: () -> Unit) {
        val userId = FirebaseService.auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users/$userId/nivel")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nuevoNivel = snapshot.getValue(String::class.java)
                    if (nuevoNivel != null) {
                        val prefs = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                        prefs.edit().putString("user_level", nuevoNivel).apply()
                        Log.d("ModulosFragment", "Nuevo nivel guardado: $nuevoNivel")
                        parentFragmentManager.beginTransaction().detach(this@ModulosFragment).attach(this@ModulosFragment).commit()
                    }
                    onComplete()  // Llamar callback cuando termine
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error cargando nivel: ${error.message}", Toast.LENGTH_SHORT).show()
                    onComplete() // También lo llamamos aquí para evitar bloqueos
                }
            })
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            onComplete()
        }
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
        ajustarAlturaListView(binding.listViewBasico)
        ajustarAlturaListView(binding.listViewIntermedio)
        ajustarAlturaListView(binding.listViewAvanzado)

        // Expansión y colapso para Básico
        binding.tituloBasico.setOnClickListener {
            toggleExpandCollapse(binding.listViewBasico)
        }

        // Expansión y colapso para Intermedio
        binding.tituloIntermedio.setOnClickListener {
            toggleExpandCollapse(binding.listViewIntermedio)
        }

        // Expansión y colapso para Avanzado
        binding.tituloAvanzado.setOnClickListener {
            toggleExpandCollapse(binding.listViewAvanzado)
        }
        // Asignar un Listener para el clic en el ListView de módulos básicos
        binding.listViewBasico.setOnItemClickListener { _, _, position, _ ->
            val moduloSeleccionado = modulosBasicos[position]
            irAActividadDetalle(moduloSeleccionado)
        }

        // Asignar un Listener para el clic en el ListView de módulos intermedios
        binding.listViewIntermedio.setOnItemClickListener { _, _, position, _ ->
            val moduloSeleccionado = modulosIntermedios[position]
            irAActividadDetalle(moduloSeleccionado)
        }

        // Asignar un Listener para el clic en el ListView de módulos avanzados
        binding.listViewAvanzado.setOnItemClickListener { _, _, position, _ ->
            val moduloSeleccionado = modulosAvanzados[position]
            irAActividadDetalle(moduloSeleccionado)
        }

        // Bloquear los módulos que no sean accesibles según el nivel del usuario
                    // Aquí accedemos al nivel del usuario
                    val nivelUsuario = loadUserLevelFromSharedPreferences()

                    // Bloquear los módulos intermedios si el nivel no es "intermedio"
                    if (nivelUsuario != "intermedio") {
                        binding.listViewIntermedio.isEnabled = false
                        binding.listViewIntermedio.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                        binding.tituloIntermedio.setBackgroundColor(Color.parseColor("#555555"))
                    }
                    if(nivelUsuario == "intermedio"){
                        binding.listViewIntermedio.isEnabled = true
                        binding.listViewIntermedio.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        binding.tituloIntermedio.setBackgroundColor(Color.parseColor("#F28705"))
                    }

                    // Bloquear los módulos avanzados si el nivel no es "avanzado"
                    if (nivelUsuario != "avanzado") {
                        binding.listViewAvanzado.isEnabled = false
                        binding.listViewAvanzado.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                        binding.tituloAvanzado.setBackgroundColor(Color.parseColor("#555555"))
                    }
                    if (nivelUsuario == "avanzado") {
                        binding.listViewAvanzado.isEnabled = true
                        binding.listViewAvanzado.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        binding.tituloAvanzado.setBackgroundColor(Color.parseColor("#F24B0F"))
                    }


    }
    private fun toggleExpandCollapse(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
    private fun ajustarAlturaListView(listView: ListView) {
        val adapter = listView.adapter ?: return

        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem = adapter.getView(i, null, listView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (adapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }
    private fun irAActividadDetalle(modulo: Modulo) {
        // Convertir los objetos en JSON
        val leccionesJson = Gson().toJson(modulo.lecciones)
        val quizJson = Gson().toJson(modulo.quiz)
        val videoJson = Gson().toJson(modulo.video)
        val intent = Intent(requireContext(), DetalleModuloActivity::class.java).apply {
            putExtra("TITULO", modulo.titulo)
            putExtra("DESCRIPCION", modulo.descripcion)
            putExtra("LECCIONES", leccionesJson)
            putExtra("QUIZ", quizJson)
            putExtra("VIDEO", videoJson)
        }
        detalleLauncher.launch(intent)

    }

}
