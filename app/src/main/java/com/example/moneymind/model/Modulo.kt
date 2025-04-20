package com.example.moneymind.model

data class Modulo(
    val titulo: String = "",
    val nivel: String = "",
    val descripcion: String = "",
    val lecciones: List<Leccion> = listOf(),
    val quiz: Quiz = Quiz(),
    val video: Video = Video()
)
