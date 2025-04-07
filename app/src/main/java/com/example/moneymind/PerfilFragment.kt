package com.example.moneymind.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moneymind.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí puedes acceder a los elementos del layout
        binding.textViewNombreUsuario.text = "Sergio Ortiz"
        binding.buttonCerrarSesion.setOnClickListener {
            // Acción para cerrar sesión
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
