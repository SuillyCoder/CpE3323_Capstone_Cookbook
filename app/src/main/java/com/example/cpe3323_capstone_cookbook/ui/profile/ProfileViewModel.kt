package com.example.cpe3323_capstone_cookbook.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpe3323_capstone_cookbook.data.AuthRepository
import com.example.cpe3323_capstone_cookbook.data.RecentActivity
import com.example.cpe3323_capstone_cookbook.data.RecentActivityRepository
import com.example.cpe3323_capstone_cookbook.data.RecipeRepository
import com.example.cpe3323_capstone_cookbook.data.SavedRepository
import com.example.cpe3323_capstone_cookbook.data.User
import com.example.cpe3323_capstone_cookbook.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileStats(
    val recipeCount: Int = 0,
    val savedCount: Int = 0,
    val myRecipesCount: Int = 0,
    val followingCount: Int = 0
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val user: User,
        val stats: ProfileStats,
        val recentActivity: List<RecentActivity>
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private var savedRepository: SavedRepository? = null,
    private var recentActivityRepository: RecentActivityRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun setSavedRepository(repo: SavedRepository) {
        savedRepository = repo
    }

    fun setRecentActivityRepository(repo: RecentActivityRepository) {
        recentActivityRepository = repo
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val userId = authRepository.currentUserId
            if (userId == null) {
                _uiState.value = ProfileUiState.Error("Not logged in")
                return@launch
            }

            val userResult = userRepository.getUser(userId)
            val user = userResult.getOrNull() ?: User(
                id = userId,
                name = authRepository.currentUserName ?: "User",
                email = authRepository.currentUserEmail ?: ""
            )

            val recipeCount = recipeRepository.getRecipesForUser(userId)
                .first()
                .getOrNull()
                ?.size ?: 0
            val savedCount = savedRepository?.getSavedCount(userId) ?: 0

            val recent = recentActivityRepository?.getRecent() ?: emptyList()
            _uiState.value = ProfileUiState.Success(
                user = user.copy(id = userId),
                stats = ProfileStats(
                    recipeCount = recipeCount,
                    savedCount = savedCount,
                    myRecipesCount = recipeCount,
                    followingCount = 0
                ),
                recentActivity = recent
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.logout()
        onLoggedOut()
    }
}
