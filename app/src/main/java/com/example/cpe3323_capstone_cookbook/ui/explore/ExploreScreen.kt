package com.example.cpe3323_capstone_cookbook.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cpe3323_capstone_cookbook.data.CUISINE_OPTIONS
import com.example.cpe3323_capstone_cookbook.data.Recipe
import com.example.cpe3323_capstone_cookbook.ui.components.RecipeCard
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeUiState
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeViewModel
import com.example.cpe3323_capstone_cookbook.ui.theme.BurntOrange
import com.example.cpe3323_capstone_cookbook.ui.theme.CreamBackground
import com.example.cpe3323_capstone_cookbook.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedIds by viewModel.savedRecipeIds.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("All") }
    val cuisines = remember { listOf("All") + CUISINE_OPTIONS }

    LaunchedEffect(Unit) {
        viewModel.ensureInitialized(context)
        viewModel.loadSavedIds()
    }

    Scaffold(
        containerColor = CreamBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = BurntOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add recipe")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
            )

            val recipeCount = (uiState as? RecipeUiState.Success)?.recipes?.size ?: 0
            Text(
                text = "Discover $recipeCount recipes worldwide",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes, cuisines, chefs...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cuisines.forEach { cuisine ->
                    val selected = selectedCuisine == cuisine
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCuisine = cuisine },
                        label = { Text(cuisine) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BurntOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            when (val state = uiState) {
                is RecipeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BurntOrange)
                    }
                }

                is RecipeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}")
                    }
                }

                is RecipeUiState.Success -> {
                    val filtered = state.recipes.filter { recipe ->
                        val matchesCuisine = selectedCuisine == "All" ||
                            recipe.cuisine.equals(selectedCuisine, ignoreCase = true)
                        val query = searchQuery.trim().lowercase()
                        val matchesSearch = query.isBlank() ||
                            recipe.title.lowercase().contains(query) ||
                            recipe.description.lowercase().contains(query) ||
                            recipe.cuisine.lowercase().contains(query) ||
                            recipe.authorName.lowercase().contains(query) ||
                            recipe.ingredients.any { it.lowercase().contains(query) }
                        matchesCuisine && matchesSearch
                    }

                    Text(
                        text = "${filtered.size} results",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCuisine != "All")
                                    "No recipes match your search"
                                else "No recipes to explore yet",
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
                            items(filtered, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    isSaved = savedIds.contains(recipe.id),
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
