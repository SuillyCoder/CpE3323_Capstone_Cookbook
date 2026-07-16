package com.example.cpe3323_capstone_cookbook.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String? get() = auth.currentUser?.uid

    suspend fun register(name: String, email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("User creation failed")
        val userDoc = hashMapOf(
            "name" to name,
            "email" to email,
            "profileImageUrl" to "",
            "role" to "user"
        )
        firestore.collection("users").document(uid).set(userDoc).await()
    }

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    fun logout() = auth.signOut()
}