package com.example.cpe3323_capstone_cookbook.ui

import androidx.compose.runtime.Composable
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeListMode
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeListScreen

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    RecipeListScreen(
        mode = RecipeListMode.MINE,
        onAddClick = onAddClick,
        onRecipeClick = onRecipeClick
    )
}