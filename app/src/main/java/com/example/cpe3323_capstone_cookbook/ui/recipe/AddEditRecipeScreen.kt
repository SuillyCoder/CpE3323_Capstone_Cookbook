package com.example.cpe3323_capstone_cookbook.ui.recipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cpe3323_capstone_cookbook.data.CUISINE_OPTIONS
import com.example.cpe3323_capstone_cookbook.data.DIFFICULTY_OPTIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    recipeId: String?,
    authorId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    val isEditMode = recipeId != null
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf("") }
    var instructions by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    var existingAuthorId by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("Other") }
    var cookTime by remember { mutableStateOf("30") }
    var difficulty by remember { mutableStateOf("Easy") }
    var cuisineExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var isLoadingExisting by remember { mutableStateOf(isEditMode) }
    val saveState by viewModel.saveState.collectAsState()

    // Prefill the form when editing an existing recipe.
    LaunchedEffect(recipeId) {
        if (recipeId != null && authorId != null) {
            val existing = viewModel.getRecipeById(authorId, recipeId)
            if (existing != null) {
                title = existing.title
                description = existing.description
                ingredients.clear()
                ingredients.addAll(existing.ingredients.ifEmpty { listOf("") })
                instructions = existing.instructions
                existingImageUrl = existing.imageUrl
                existingAuthorId = existing.authorId
                cuisine = existing.cuisine.ifBlank { "Other" }
                cookTime = existing.cookTimeMinutes.toString()
                difficulty = existing.difficulty.ifBlank { "Easy" }
            }
            isLoadingExisting = false
        }
    }

    // Navigate back once the save completes successfully.
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            viewModel.clearSaveState()
            onDone()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) pickedImageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Recipe" else "Add Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoadingExisting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Picture ---
            val imageModel: Any? = pickedImageUri ?: existingImageUrl.ifBlank { null }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Recipe photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Text("Tap to add a photo")
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = cuisineExpanded,
                    onExpandedChange = { cuisineExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = cuisine,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuisine") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cuisineExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cuisineExpanded,
                        onDismissRequest = { cuisineExpanded = false }
                    ) {
                        CUISINE_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    cuisine = option
                                    cuisineExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = cookTime,
                    onValueChange = { cookTime = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Minutes") },
                    modifier = Modifier.weight(1f)
                )
            }

            ExposedDropdownMenuBox(
                expanded = difficultyExpanded,
                onExpandedChange = { difficultyExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Difficulty") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = { difficultyExpanded = false }
                ) {
                    DIFFICULTY_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                difficulty = option
                                difficultyExpanded = false
                            }
                        )
                    }
                }
            }

            // --- Ingredients (dynamic list) ---
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            ingredients.forEachIndexed { index, value ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue -> ingredients[index] = newValue },
                        label = { Text("Ingredient ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { if (ingredients.size > 1) ingredients.removeAt(index) }
                    ) {
                        Icon(Icons.Filled.RemoveCircle, contentDescription = "Remove ingredient")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { ingredients.add("") }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add ingredient")
                }
                Text("Add ingredient", modifier = Modifier.padding(top = 12.dp))
            }

            // --- Instructions ---
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            if (saveState is SaveState.Error) {
                Text(
                    text = (saveState as SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.saveRecipe(
                        existingRecipeId = recipeId,
                        title = title,
                        description = description,
                        ingredients = ingredients.toList(),
                        instructions = instructions,
                        imageUri = pickedImageUri,
                        existingImageUrl = existingImageUrl,
                        existingAuthorId = existingAuthorId,
                        cuisine = cuisine,
                        cookTimeMinutes = cookTime.toIntOrNull() ?: 30,
                        difficulty = difficulty,
                        context = context
                    )
                },
                enabled = saveState !is SaveState.Saving && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saveState is SaveState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Save Changes" else "Add Recipe")
                }
            }
        }
    }
}