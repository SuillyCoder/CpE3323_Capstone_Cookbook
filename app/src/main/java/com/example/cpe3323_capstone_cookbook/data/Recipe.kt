package com.example.cpe3323_capstone_cookbook.data

data class Recipe (

    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
