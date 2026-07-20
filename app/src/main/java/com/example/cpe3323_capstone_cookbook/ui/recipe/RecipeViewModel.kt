package com.example.cpe3323_capstone_cookbook.ui.recipe


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpe3323_capstone_cookbook.data.AuthRepository
import com.example.cpe3323_capstone_cookbook.data.Recipe
import com.example.cpe3323_capstone_cookbook.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeUiState {
    data object Loading : RecipeUiState
    data class Success(val recipes: List<Recipe>) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Success : SaveState
    data class Error(val message: String) : SaveState
}

class RecipeViewModel(
    private val repository: RecipeRepository = RecipeRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Drives RecipeListScreen (ALL recipes)
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // Drives "My Recipes" filter
    private val _myRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val myRecipesState: StateFlow<RecipeUiState> = _myRecipesState.asStateFlow()

    // Drives save operation state for AddEditRecipeScreen
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // Drives one-off feedback (snackbars, nav-back-on-success) after add/update/delete
    private val _actionState = MutableStateFlow<Result<Unit>?>(null)
    val actionState: StateFlow<Result<Unit>?> = _actionState.asStateFlow()

    init {
        observeRecipes()
        observeMyRecipes()
    }

    private fun observeRecipes() {
        viewModelScope.launch {
            repository.getRecipes().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { RecipeUiState.Success(it) },
                    onFailure = { RecipeUiState.Error(it.message ?: "Failed to load recipes") }
                )
            }
        }
    }

    private fun observeMyRecipes() {
        viewModelScope.launch {
            repository.getRecipes().collect { result ->
                val currentUserId = authRepository.currentUserId
                _myRecipesState.value = result.fold(
                    onSuccess = { recipes ->
                        val myRecipes = if (currentUserId != null) {
                            recipes.filter { it.authorId == currentUserId }
                        } else {
                            emptyList()
                        }
                        RecipeUiState.Success(myRecipes)
                    },
                    onFailure = { RecipeUiState.Error(it.message ?: "Failed to load my recipes") }
                )
            }
        }
    }

    suspend fun getRecipeById(recipeId: String): Recipe? {
        return repository.getRecipe(recipeId).getOrNull()
    }

    fun saveRecipe(
        existingRecipeId: String?,
        title: String,
        description: String,
        ingredients: List<String>,
        instructions: String,
        imageUri: Uri?,
        existingImageUrl: String,
        existingAuthorId: String
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            // TODO: Upload image to Firebase Storage if imageUri is not null
            // For now, we'll use existingImageUrl or a placeholder
            val finalImageUrl = if (imageUri != null) {
                // Upload image and get URL
                // This requires Firebase Storage implementation
                // For now, use placeholder
                existingImageUrl
            } else {
                existingImageUrl
            }

            val recipe = Recipe(
                id = existingRecipeId ?: "",
                title = title,
                description = description,
                ingredients = ingredients.filter { it.isNotBlank() },
                instructions = instructions,
                imageUrl = finalImageUrl,
                authorId = existingAuthorId.ifEmpty { authRepository.currentUserId ?: "" },
                timestamp = System.currentTimeMillis()
            )

            val result = if (existingRecipeId != null) {
                repository.updateRecipe(recipe)
            } else {
                repository.addRecipe(recipe).map { }
            }

            _saveState.value = result.fold(
                onSuccess = { SaveState.Success },
                onFailure = { SaveState.Error(it.message ?: "Failed to save recipe") }
            )
        }
    }

    fun clearSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _actionState.value = repository.addRecipe(recipe).map { }
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _actionState.value = repository.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _actionState.value = repository.deleteRecipe(recipeId)
        }
    }

    fun clearActionState() {
        _actionState.value = null
    }
}