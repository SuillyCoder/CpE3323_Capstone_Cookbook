package com.example.cpe3323_capstone_cookbook.data

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val cuisine: String = "",
    val cookTimeMinutes: Int = 30,
    val difficulty: String = "Easy",
    val timestamp: Long = System.currentTimeMillis()
)

val CUISINE_OPTIONS = listOf("Italian", "Japanese", "French", "Mexican", "Indian", "American", "Other")
val DIFFICULTY_OPTIONS = listOf("Easy", "Medium", "Hard")
