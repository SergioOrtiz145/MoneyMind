package com.example.moneymind.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.moneymind.R

class ModuloAdapter(context: Context, modulos: List<Modulo>) : ArrayAdapter<Modulo>(context, 0, modulos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_modulo, parent, false)

        val modulo = getItem(position)
        val tituloModulo = view.findViewById<TextView>(R.id.tituloModulo)
        tituloModulo.text = modulo?.titulo

        return view
    }
}
