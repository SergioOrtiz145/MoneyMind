package com.example.moneymind.model

import com.google.firebase.Timestamp

data class Publicacion(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val autor: String = "",
    val fecha: Long = 0L
)

