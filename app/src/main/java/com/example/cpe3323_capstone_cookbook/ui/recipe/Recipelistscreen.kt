package com.example.cpe3323_capstone_cookbook.ui.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cpe3323_capstone_cookbook.data.Recipe


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recipes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.addRecipe(
                    Recipe(
                        title = "Test Recipe",
                        description = "Created from the (+) button",
                        ingredients = listOf("Test ingredient"),
                        instructions = "Step 1: verify Create works."
                    )
                )
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add test recipe")
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is RecipeUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is RecipeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}")
                }
            }

            is RecipeUiState.Success -> {
                if (state.recipes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No recipes yet — tap + to add one")
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(state.recipes, key = { it.id }) { recipe ->
                            ListItem(
                                headlineContent = { Text(recipe.title) },
                                supportingContent = { Text(recipe.description) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.deleteRecipe(recipe.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete ${recipe.title}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
