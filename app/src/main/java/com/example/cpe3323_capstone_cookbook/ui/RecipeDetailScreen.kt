package com.example.cpe3323_capstone_cookbook.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    authorId: String,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var recipe by remember { mutableStateOf<com.example.cpe3323_capstone_cookbook.data.Recipe?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(recipeId, authorId) {
        recipe = viewModel.getRecipeById(authorId, recipeId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val current = recipe
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Recipe not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (current.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = current.imageUrl,
                        contentDescription = current.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.RestaurantMenu, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }

            Text(current.title, style = MaterialTheme.typography.headlineSmall)
            Text(current.description, style = MaterialTheme.typography.bodyMedium)

            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            current.ingredients.forEach { ingredient ->
                Text("• $ingredient", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Instructions", style = MaterialTheme.typography.titleMedium)
            Text(current.instructions, style = MaterialTheme.typography.bodyMedium)
        }
    }
}