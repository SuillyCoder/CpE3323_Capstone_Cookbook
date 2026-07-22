package com.example.cpe3323_capstone_cookbook.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getUser(userId: String): Result<User?> = runCatching {
        val snapshot = firestore.collection("users").document(userId).get().await()
        snapshot.toObject<User>()?.copy(id = userId)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).update(updates).await()
    }
}
