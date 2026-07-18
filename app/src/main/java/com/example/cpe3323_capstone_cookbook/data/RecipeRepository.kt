package com.example.cpe3323_capstone_cookbook.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val recipesCollection = firestore.collection("recipes")

    /** Create */
    suspend fun addRecipe(recipe: Recipe): Result<String> {
        return try {
            val docRef = recipesCollection.document()
            val recipeWithId = recipe.copy(id = docRef.id)
            docRef.set(recipeWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Read (live stream — use in RecipeListScreen) */
    fun getRecipes(): Flow<Result<List<Recipe>>> = callbackFlow {
        val listener = recipesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val recipes = snapshot?.documents?.mapNotNull { it.toObject<Recipe>() } ?: emptyList()
            trySend(Result.success(recipes))
        }
        awaitClose { listener.remove() }
    }

    /** Read (single item — use in RecipeDetailScreen / AddEditRecipeScreen) */
    suspend fun getRecipe(recipeId: String): Result<Recipe?> {
        return try {
            val snapshot = recipesCollection.document(recipeId).get().await()
            Result.success(snapshot.toObject<Recipe>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update */
    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            require(recipe.id.isNotBlank()) { "Recipe id must not be blank for update" }
            recipesCollection.document(recipe.id).set(recipe).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete */
    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
 