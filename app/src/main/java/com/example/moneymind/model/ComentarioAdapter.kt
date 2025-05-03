package com.example.moneymind.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymind.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComentarioAdapter(private val lista: List<Comentario>) :
    RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    inner class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val autor: TextView = itemView.findViewById(R.id.textAutorComentario)
        val texto: TextView = itemView.findViewById(R.id.textTextoComentario)
        val fecha: TextView = itemView.findViewById(R.id.textFechaComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = lista[position]
        holder.autor.text = comentario.autor
        holder.texto.text = comentario.texto
        holder.fecha.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(comentario.fecha))
    }

    override fun getItemCount(): Int = lista.size
}
