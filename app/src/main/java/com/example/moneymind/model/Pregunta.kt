package com.example.moneymind.model

data class Pregunta(val texto: String = "", val opciones: List<String> = listOf(), val respuestaCorrecta: String = "")
