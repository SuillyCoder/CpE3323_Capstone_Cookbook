package com.example.cpe3323_capstone_cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cpe3323_capstone_cookbook.data.AuthRepository
import com.example.cpe3323_capstone_cookbook.ui.auth.LoginScreen
import com.example.cpe3323_capstone_cookbook.ui.auth.RegisterScreen
import com.example.cpe3323_capstone_cookbook.ui.navigation.MainScaffold
import com.example.cpe3323_capstone_cookbook.ui.recipe.AddEditRecipeScreen
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeDetailScreen
import com.example.cpe3323_capstone_cookbook.ui.theme.CpE3323_Capstone_CookbookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CpE3323_Capstone_CookbookTheme {
                val navController = rememberNavController()
                val authRepository = remember { AuthRepository() }
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    startDestination = if (authRepository.isLoggedIn) "main" else "login"
                }

                if (startDestination != null) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }

                        composable("main") {
                            MainScaffold(
                                onAddClick = { navController.navigate("recipeForm") },
                                onEditClick = { recipe ->
                                    navController.navigate(
                                        "recipeForm?authorId=${recipe.authorId}&recipeId=${recipe.id}"
                                    )
                                },
                                onViewClick = { recipe ->
                                    navController.navigate(
                                        "recipeDetail/${recipe.authorId}/${recipe.id}"
                                    )
                                },
                                onSignOut = {
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "recipeForm?authorId={authorId}&recipeId={recipeId}",
                            arguments = listOf(
                                navArgument("authorId") {
                                    type = NavType.StringType
                                    defaultValue = null
                                    nullable = true
                                },
                                navArgument("recipeId") {
                                    type = NavType.StringType
                                    defaultValue = null
                                    nullable = true
                                }
                            )
                        ) { backStackEntry ->
                            AddEditRecipeScreen(
                                recipeId = backStackEntry.arguments?.getString("recipeId"),
                                authorId = backStackEntry.arguments?.getString("authorId"),
                                onDone = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "recipeDetail/{authorId}/{recipeId}",
                            arguments = listOf(
                                navArgument("authorId") { type = NavType.StringType },
                                navArgument("recipeId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            RecipeDetailScreen(
                                recipeId = backStackEntry.arguments?.getString("recipeId") ?: "",
                                authorId = backStackEntry.arguments?.getString("authorId") ?: "",
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
