package com.example.dementenatural

data class Sale(
    val saleId: String = "",
    val email: String = "",
    val products: Map<String, Int> = emptyMap(),
    val total: Double = 0.0
)
