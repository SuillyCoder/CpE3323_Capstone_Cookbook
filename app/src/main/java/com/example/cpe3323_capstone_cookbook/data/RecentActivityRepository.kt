package com.example.cpe3323_capstone_cookbook.data

import android.content.Context
import android.content.SharedPreferences

data class RecentActivity(
    val recipeId: String = "",
    val authorId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val viewedAt: Long = System.currentTimeMillis()
)

class RecentActivityRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("recent_activity", Context.MODE_PRIVATE)

    fun addViewed(recipe: Recipe) {
        val current = getRecent().toMutableList()
        current.removeAll { it.recipeId == recipe.id }
        current.add(
            0,
            RecentActivity(
                recipeId = recipe.id,
                authorId = recipe.authorId,
                title = recipe.title,
                imageUrl = recipe.imageUrl,
                viewedAt = System.currentTimeMillis()
            )
        )
        val serialized = current.take(10).joinToString(";;") { activity ->
            listOf(
                activity.recipeId,
                activity.authorId,
                activity.title.replace(";;", " "),
                activity.imageUrl,
                activity.viewedAt.toString()
            ).joinToString("|")
        }
        prefs.edit().putString("items", serialized).apply()
    }

    fun getRecent(): List<RecentActivity> {
        val raw = prefs.getString("items", null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size < 5) return@mapNotNull null
            RecentActivity(
                recipeId = parts[0],
                authorId = parts[1],
                title = parts[2],
                imageUrl = parts[3],
                viewedAt = parts[4].toLongOrNull() ?: 0L
            )
        }
    }
}
