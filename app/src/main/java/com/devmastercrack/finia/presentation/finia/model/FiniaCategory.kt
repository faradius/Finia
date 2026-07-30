package com.devmastercrack.finia.presentation.finia.model

data class FiniaCategory(val nombre: String, val emoji: String)

val DefaultCategories = listOf(
    FiniaCategory("Comida", "🍔"),
    FiniaCategory("Transporte", "🚗"),
    FiniaCategory("Hogar", "🏠"),
    FiniaCategory("Trabajo", "💼"),
    FiniaCategory("Ocio", "🎉"),
    FiniaCategory("Salud", "❤️"),
)

val PeopleOptions = listOf("Carlos", "Sofía", "Mateo")

val CategoryKeywords: Map<String, List<String>> = mapOf(
    "Comida" to listOf("pizza", "cena", "comida", "restaurante", "super", "supermercado", "almuerzo", "desayuno", "café", "cafe", "navidad", "tacos"),
    "Transporte" to listOf("uber", "taxi", "gasolina", "transporte", "metro", "camion", "didi", "estacionamiento"),
    "Hogar" to listOf("renta", "luz", "agua", "gas", "hogar", "muebles", "limpieza"),
    "Trabajo" to listOf("oficina", "trabajo", "proyecto", "cliente", "material"),
    "Ocio" to listOf("cine", "netflix", "concierto", "ocio", "fiesta", "juego", "spotify"),
    "Salud" to listOf("farmacia", "doctor", "medicina", "salud", "gimnasio", "hospital"),
)
