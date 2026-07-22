package com.example.cpe3323_capstone_cookbook.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONObject

class SavedRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun storageKey(userId: String) = "saved_$userId"

    fun getSavedRecipes(userId: String): Flow<Result<List<Recipe>>> = callbackFlow {
        fun emitSaved() {
            trySend(Result.success(loadSaved(userId)))
        }

        emitSaved()

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == storageKey(userId)) {
                emitSaved()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    suspend fun toggleSaved(userId: String, recipe: Recipe): Result<Boolean> = runCatching {
        val current = loadSaved(userId).toMutableList()
        val index = current.indexOfFirst { it.id == recipe.id }
        if (index >= 0) {
            current.removeAt(index)
            persistSaved(userId, current)
            false
        } else {
            current.add(0, recipe)
            persistSaved(userId, current)
            true
        }
    }

    suspend fun isSaved(userId: String, recipeId: String): Boolean = runCatching {
        loadSaved(userId).any { it.id == recipeId }
    }.getOrDefault(false)

    fun getSavedCount(userId: String): Int = loadSaved(userId).size

    private fun loadSaved(userId: String): List<Recipe> {
        val raw = prefs.getString(storageKey(userId), null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            JSONArray(raw).let { array ->
                (0 until array.length()).mapNotNull { index ->
                    array.optJSONObject(index)?.toRecipe()
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun persistSaved(userId: String, recipes: List<Recipe>) {
        val array = JSONArray()
        recipes.forEach { recipe -> array.put(recipe.toJson()) }
        prefs.edit().putString(storageKey(userId), array.toString()).apply()
    }

    private fun Recipe.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("instructions", instructions)
        put("imageUrl", imageUrl)
        put("authorId", authorId)
        put("authorName", authorName)
        put("cuisine", cuisine)
        put("cookTimeMinutes", cookTimeMinutes)
        put("difficulty", difficulty)
        put("timestamp", timestamp)
        put("ingredients", JSONArray(ingredients))
    }

    private fun JSONObject.toRecipe(): Recipe? = runCatching {
        val ingredientsArray = optJSONArray("ingredients") ?: JSONArray()
        Recipe(
            id = getString("id"),
            title = optString("title", ""),
            description = optString("description", ""),
            ingredients = (0 until ingredientsArray.length()).map { ingredientsArray.getString(it) },
            instructions = optString("instructions", ""),
            imageUrl = optString("imageUrl", ""),
            authorId = optString("authorId", ""),
            authorName = optString("authorName", ""),
            cuisine = optString("cuisine", ""),
            cookTimeMinutes = optInt("cookTimeMinutes", 30),
            difficulty = optString("difficulty", "Easy"),
            timestamp = optLong("timestamp", System.currentTimeMillis())
        )
    }.getOrNull()

    companion object {
        private const val PREFS_NAME = "saved_recipes"

        @Volatile
        private var instance: SavedRepository? = null

        fun getInstance(context: Context): SavedRepository {
            return instance ?: synchronized(this) {
                instance ?: SavedRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
