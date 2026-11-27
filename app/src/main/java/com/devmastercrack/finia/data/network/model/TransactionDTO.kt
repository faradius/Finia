package com.devmastercrack.finia.data.network.model

data class TransactionDTO(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val date: Long = 0,
    val frequency: String = "",
    val isPrivate: Boolean = true,
    val category: String = "",
    val tags: List<String> = emptyList()
)
