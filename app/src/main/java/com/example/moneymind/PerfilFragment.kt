package com.example.moneymind.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.moneymind.R
import com.example.moneymind.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private var completarNivel:Boolean? = false
    private var modulos3:Boolean? = false
    private var primerModulo:Boolean? = false
    private var primerComentario:Boolean? = false
    private var primeraPublicacion:Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            val nombre = snapshot.child("name").getValue(String::class.java)
            val apellido = snapshot.child("lastName").getValue(String::class.java)
            val nivel = snapshot.child("nivel").getValue(String::class.java)
            val conocimientos = snapshot.child("conocimientos").children.mapNotNull { it.getValue(String::class.java) }
            completarNivel = snapshot.child("completarNivel").getValue(Boolean::class.java)
            modulos3 = snapshot.child("modulos3").getValue(Boolean::class.java)
            primerModulo = snapshot.child("primerModulo").getValue(Boolean::class.java)
            primerComentario = snapshot.child("primerComentario").getValue(Boolean::class.java)
            primeraPublicacion = snapshot.child("primeraPublicacion").getValue(Boolean::class.java)
            binding.nombre.text = "¡Hola, $nombre $apellido!"
            binding.nivel.text = "Eres nivel $nivel"
            mostrarIntereses(conocimientos)
            pintarLogros()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun mostrarIntereses(intereses: List<String>) {
        val layout = binding.layoutIntereses
        layout.removeAllViews()
        val fuente = ResourcesCompat.getFont(requireContext(), R.font.oswald_variablefont_wght)

        for (interes in intereses) {
            val chip = TextView(requireContext()).apply {
                text = interes
                setPadding(40, 20, 40, 20)
                textSize = 20f
                typeface = fuente
                setTextColor(Color.parseColor("#000000"))
                fontFeatureSettings
                setBackgroundResource(R.drawable.bg_chip)
                gravity = Gravity.CENTER
                maxLines = 2
                minWidth = 0
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(20, 12, 20, 12)
            }

            chip.layoutParams = params
            layout.addView(chip)
        }
    }
    private fun pintarLogros(){
        if(completarNivel == true){
            binding.cardLogro3.setBackgroundColor(Color.parseColor("#9EF8EE"))
        }
        if(modulos3 == true){
            binding.cardLogro2.setBackgroundColor(Color.parseColor("#9EF8EE"))
        }
        if(primerModulo == true){
            binding.cardLogro1.setBackgroundColor(Color.parseColor("#9EF8EE"))
        }
        if(primerComentario == true){
            binding.cardLogro6.setBackgroundColor(Color.parseColor("#9EF8EE"))
        }
        if(primeraPublicacion == true){
            binding.cardLogro4.setBackgroundColor(Color.parseColor("#9EF8EE"))
        }
    }
}
