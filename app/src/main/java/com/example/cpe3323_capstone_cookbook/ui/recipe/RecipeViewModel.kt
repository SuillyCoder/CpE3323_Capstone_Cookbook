package com.example.cpe3323_capstone_cookbook.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class RecipeViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    // Drives RecipeListScreen
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // Drives one-off feedback (snackbars, nav-back-on-success) after add/update/delete
    private val _actionState = MutableStateFlow<Result<Unit>?>(null)
    val actionState: StateFlow<Result<Unit>?> = _actionState.asStateFlow()

    init {
        observeRecipes()
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

    /** Call after handling a snackbar/nav so the same result doesn't re-fire. */
    fun clearActionState() {
        _actionState.value = null
    }
}