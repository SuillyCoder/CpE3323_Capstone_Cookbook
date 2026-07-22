package com.example.cpe3323_capstone_cookbook.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cpe3323_capstone_cookbook.data.Recipe
import com.example.cpe3323_capstone_cookbook.ui.components.RecipeCard
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeUiState
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeViewModel
import com.example.cpe3323_capstone_cookbook.ui.theme.BurntOrange
import com.example.cpe3323_capstone_cookbook.ui.theme.CreamBackground
import com.example.cpe3323_capstone_cookbook.ui.theme.TextSecondary

@Composable
fun SavedScreen(
    onRecipeClick: (Recipe) -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    val savedState by viewModel.savedRecipesState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.ensureInitialized(context)
        viewModel.loadSavedRecipes()
    }

    Scaffold(containerColor = CreamBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Saved",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
            )
            Text(
                text = "Your bookmarked recipes",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (val state = savedState) {
                is RecipeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BurntOrange)
                    }
                }

                is RecipeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Could not load saved recipes.\nPlease try again later.",
                            color = TextSecondary
                        )
                    }
                }

                is RecipeUiState.Success -> {
                    if (state.recipes.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No saved recipes yet.\nTap the heart on any recipe to save it.",
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.recipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    isSaved = true,
                                    onClick = { onRecipeClick(recipe) },
                                    onToggleSave = { viewModel.toggleSaved(recipe) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
