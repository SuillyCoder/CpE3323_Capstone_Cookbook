package com.example.cpe3323_capstone_cookbook.ui.recipe

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpe3323_capstone_cookbook.data.AuthRepository
import com.example.cpe3323_capstone_cookbook.data.FirebaseStorageUtils
import com.example.cpe3323_capstone_cookbook.data.Recipe
import com.example.cpe3323_capstone_cookbook.data.RecipeRepository
import com.example.cpe3323_capstone_cookbook.data.copyImageToInternalStorage
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
    private val authRepository: AuthRepository = AuthRepository(),
    private val storageUtils: FirebaseStorageUtils = FirebaseStorageUtils()
) : ViewModel() {

    companion object {
        private const val TAG = "RecipeViewModel"
    }

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

    // Store context for local storage operations (set from AddEditRecipeScreen)
    private var appContext: Context? = null

    fun setContext(context: Context) {
        appContext = context
    }

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
            val currentUserId = authRepository.currentUserId
            if (currentUserId == null) {
                _myRecipesState.value = RecipeUiState.Success(emptyList())
                return@launch
            }
            repository.getRecipesForUser(currentUserId).collect { result ->
                _myRecipesState.value = result.fold(
                    onSuccess = { RecipeUiState.Success(it) },
                    onFailure = { RecipeUiState.Error(it.message ?: "Failed to load my recipes") }
                )
            }
        }
    }

    suspend fun getRecipeById(authorId: String, recipeId: String): Recipe? {
        return repository.getRecipe(authorId, recipeId).getOrNull()
    }

    fun saveRecipe(
        existingRecipeId: String?,
        title: String,
        description: String,
        ingredients: List<String>,
        instructions: String,
        imageUri: Uri?,
        existingImageUrl: String,
        existingAuthorId: String,
        context: Context  // Add context parameter
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            try {
                val authorId = existingAuthorId.ifEmpty { authRepository.currentUserId ?: "" }
                var finalImageUrl = existingImageUrl
                var uploadMethod = "existing" // Track where the image came from

                Log.d(TAG, "=== SAVING RECIPE ===")
                Log.d(TAG, "Title: $title")
                Log.d(TAG, "Author ID: $authorId")
                Log.d(TAG, "Image Uri: $imageUri")
                Log.d(TAG, "Existing Image URL: $existingImageUrl")

                // Upload image if a new one was selected
                if (imageUri != null) {
                    Log.d(TAG, "📤 New image selected, trying Firebase Storage first...")

                    if (authorId.isEmpty()) {
                        Log.e(TAG, "❌ Author ID is empty! Cannot upload.")
                        _saveState.value = SaveState.Error("User not authenticated")
                        return@launch
                    }

                    // Try Firebase Storage first
                    val uploadResult = storageUtils.uploadRecipeImage(authorId, imageUri)

                    if (uploadResult.isSuccess) {
                        finalImageUrl = uploadResult.getOrNull() ?: ""
                        uploadMethod = "firebase_storage"
                        Log.d(TAG, "✅ Image uploaded to Firebase Storage successfully!")
                        Log.d(TAG, "✅ New Image URL: $finalImageUrl")
                    } else {
                        // Firebase Storage failed - fallback to local storage
                        Log.e(TAG, "❌ Firebase Storage upload failed! Falling back to local storage...")
                        val error = uploadResult.exceptionOrNull()
                        Log.e(TAG, "Error message: ${error?.message}")

                        // Try local storage
                        try {
                            val localPath = copyImageToInternalStorage(context, imageUri)
                            if (localPath != null) {
                                finalImageUrl = localPath
                                uploadMethod = "local_storage"
                                Log.d(TAG, "✅ Image saved locally: $localPath")
                            } else {
                                Log.e(TAG, "❌ Local storage also failed!")
                                finalImageUrl = existingImageUrl // Use existing or empty
                                uploadMethod = "failed"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Local storage exception: ${e.message}")
                            finalImageUrl = existingImageUrl
                            uploadMethod = "failed"
                        }
                    }
                } else {
                    Log.d(TAG, "ℹ️ No new image selected, using existing: $finalImageUrl")
                    uploadMethod = "existing"
                }

                // Create recipe object
                val recipe = Recipe(
                    id = existingRecipeId ?: "",
                    title = title,
                    description = description,
                    ingredients = ingredients.filter { it.isNotBlank() },
                    instructions = instructions,
                    imageUrl = finalImageUrl,
                    authorId = authorId,
                    timestamp = System.currentTimeMillis()
                )

                Log.d(TAG, "📝 Recipe object created:")
                Log.d(TAG, "  - ID: ${recipe.id}")
                Log.d(TAG, "  - Title: ${recipe.title}")
                Log.d(TAG, "  - AuthorId: ${recipe.authorId}")
                Log.d(TAG, "  - ImageUrl: ${recipe.imageUrl}")
                Log.d(TAG, "  - Upload Method: $uploadMethod")

                // Save to Firestore
                val result = if (existingRecipeId != null) {
                    Log.d(TAG, "🔄 Updating existing recipe...")
                    repository.updateRecipe(recipe)
                } else {
                    Log.d(TAG, "➕ Adding new recipe...")
                    repository.addRecipe(recipe).map { }
                }

                result.onSuccess {
                    Log.d(TAG, "✅ Recipe saved successfully to Firestore!")
                }.onFailure {
                    Log.e(TAG, "❌ Failed to save recipe to Firestore!", it)
                }

                _saveState.value = result.fold(
                    onSuccess = {
                        Log.d(TAG, "=== SAVE COMPLETE (Method: $uploadMethod) ===")
                        SaveState.Success
                    },
                    onFailure = {
                        Log.e(TAG, "=== SAVE FAILED ===")
                        SaveState.Error(it.message ?: "Failed to save recipe")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error in saveRecipe!", e)
                _saveState.value = SaveState.Error(e.message ?: "Failed to save recipe")
            }
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

    fun deleteRecipe(authorId: String, recipeId: String) {
        viewModelScope.launch {
            _actionState.value = repository.deleteRecipe(authorId, recipeId)
        }
    }

    fun clearActionState() {
        _actionState.value = null
    }
}