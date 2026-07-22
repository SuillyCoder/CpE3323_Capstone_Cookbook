package com.example.cpe3323_capstone_cookbook.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val role: String = "user"
)
