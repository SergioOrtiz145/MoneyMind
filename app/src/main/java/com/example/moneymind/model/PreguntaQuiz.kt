package com.example.moneymind.model

data class PreguntaQuiz(
    val pregunta: String = "",
    val opciones: List<String> = listOf(),
    val respuesta_correcta: String = ""
)
