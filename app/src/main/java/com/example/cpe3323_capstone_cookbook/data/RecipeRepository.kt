package com.example.cpe3323_capstone_cookbook.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storageUtils: FirebaseStorageUtils = FirebaseStorageUtils() // Add this
) {
    private val usersCollection = firestore.collection("users")

    private fun recipesCollectionFor(authorId: String) =
        usersCollection.document(authorId).collection("recipes")

    suspend fun addRecipe(recipe: Recipe): Result<String> {
        return try {
            require(recipe.authorId.isNotBlank()) { "authorId must be set before creating a recipe" }
            val docRef = recipesCollectionFor(recipe.authorId).document()
            val recipeWithId = recipe.copy(id = docRef.id)
            docRef.set(recipeWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** ALL recipes across every user — for Browse. "recipes" now lives nested under each
     *  user, so a collection group query is what lets us still see everyone's at once. */
    fun getRecipes(): Flow<Result<List<Recipe>>> = callbackFlow {
        val listener = firestore.collectionGroup("recipes").addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(Result.failure(error)); return@addSnapshotListener }
            trySend(Result.success(snapshot?.documents?.mapNotNull { it.toObject<Recipe>() } ?: emptyList()))
        }
        awaitClose { listener.remove() }
    }

    /** Only one user's recipes — for "My Recipes". No client-side filtering needed anymore. */
    fun getRecipesForUser(authorId: String): Flow<Result<List<Recipe>>> = callbackFlow {
        val listener = recipesCollectionFor(authorId).addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(Result.failure(error)); return@addSnapshotListener }
            trySend(Result.success(snapshot?.documents?.mapNotNull { it.toObject<Recipe>() } ?: emptyList()))
        }
        awaitClose { listener.remove() }
    }

    suspend fun getRecipe(authorId: String, recipeId: String): Result<Recipe?> {
        return try {
            val snapshot = recipesCollectionFor(authorId).document(recipeId).get().await()
            Result.success(snapshot.toObject<Recipe>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            require(recipe.id.isNotBlank()) { "Recipe id must not be blank for update" }
            recipesCollectionFor(recipe.authorId).document(recipe.id).set(recipe).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecipe(authorId: String, recipeId: String): Result<Unit> {
        return try {
            // First get the recipe to get the image URL
            val recipe = getRecipe(authorId, recipeId).getOrNull()

            // Delete the recipe from Firestore
            recipesCollectionFor(authorId).document(recipeId).delete().await()

            // Delete the image from Firebase Storage if it exists
            recipe?.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                    storageUtils.deleteRecipeImage(imageUrl)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}