package com.example.moneymind.model

data class Leccion(
    val titulo: String = "",
    val definicion: String = "",
    val beneficios: List<String> = listOf(),
    val pasos: List<String> = listOf(),
    val consejos: List<String> = listOf(),
    val conclusion: String = ""
)
