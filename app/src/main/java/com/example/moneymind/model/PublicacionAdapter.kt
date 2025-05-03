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

class PublicacionAdapter(private val lista: List<Publicacion>,
    private val onItemClick: (Publicacion) -> Unit) :
    RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder>() {

    inner class PublicacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo = itemView.findViewById<TextView>(R.id.titulo)
        val contenido = itemView.findViewById<TextView>(R.id.contenido)
        val autor = itemView.findViewById<TextView>(R.id.autor)
        val fecha = itemView.findViewById<TextView>(R.id.fecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publicacion, parent, false)
        return PublicacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicacionViewHolder, position: Int) {
        val publicacion = lista[position]
        val pub = lista[position]
        holder.titulo.text = pub.titulo
        holder.contenido.text = pub.contenido
        holder.autor.text = pub.autor
        holder.fecha.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(pub.fecha))
        // Aquí capturamos el clic
        holder.itemView.setOnClickListener {
            onItemClick(publicacion)
        }
    }

    override fun getItemCount(): Int = lista.size
}
