package com.example.moneymind.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymind.R

class MiAdapter(private val lista: List<String>) : RecyclerView.Adapter<MiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_listado, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modulo = lista[position]  // Aquí tomamos el String de la lista
        holder.tituloModulo.text = "- ${modulo}"  // Asignamos directamente el texto al TextView
    }

    override fun getItemCount(): Int = lista.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloModulo: TextView = itemView.findViewById(R.id.tituloModulo)  // Asegúrate de que el id corresponda al de tu TextView
    }
}

