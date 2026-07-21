package com.example.cpe3323_capstone_cookbook.ui.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cpe3323_capstone_cookbook.data.Recipe
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    mode: RecipeListMode,
    onAddClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,   // now specifically means "edit"
    onViewClick: (Recipe) -> Unit,     // new
    viewModel: RecipeViewModel = viewModel()
) {
    val uiState by (if (mode == RecipeListMode.MINE) viewModel.myRecipesState else viewModel.uiState)
        .collectAsState()

    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (mode == RecipeListMode.MINE) "My Recipes" else "Browse Recipes") })
        },
        floatingActionButton = {
            if (mode == RecipeListMode.MINE) {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Add recipe")
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is RecipeUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is RecipeUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}")
                }
            }

            is RecipeUiState.Success -> {
                if (state.recipes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (mode == RecipeListMode.MINE) "No recipes yet — tap + to add one" else "No recipes to browse yet")
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(state.recipes, key = { it.id }) { recipe ->
                            ListItem(
                                leadingContent = {
                                    if (recipe.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = recipe.imageUrl,
                                            contentDescription = recipe.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Icon(Icons.Filled.RestaurantMenu, contentDescription = null, modifier = Modifier.size(48.dp))
                                    }
                                },
                                headlineContent = { Text(recipe.title) },
                                supportingContent = { Text(recipe.description) },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = { onViewClick(recipe) }) {
                                            Icon(Icons.Filled.Visibility, contentDescription = "View ${recipe.title}")
                                        }
                                        if (mode == RecipeListMode.MINE) {
                                            IconButton(onClick = { onRecipeClick(recipe) }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit ${recipe.title}")
                                            }
                                            IconButton(onClick = { recipeToDelete = recipe }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete ${recipe.title}")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    recipeToDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text("Delete recipe?") },
            text = { Text("Are you sure you want to delete \"${recipe.title}\"? This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe(recipe.authorId,recipe.id)
                    recipeToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
