package com.example.cpe3323_capstone_cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cpe3323_capstone_cookbook.ui.HomeScreen
import com.example.cpe3323_capstone_cookbook.ui.auth.LoginScreen
import com.example.cpe3323_capstone_cookbook.ui.auth.RegisterScreen
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
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            onAddClick = { navController.navigate("recipeForm") },
                            onEditClick = { recipe ->
                                navController.navigate("recipeForm?authorId=${recipe.authorId}&recipeId=${recipe.id}")
                            },
                            onViewClick = { recipe ->
                                navController.navigate("recipeDetail/${recipe.authorId}/${recipe.id}")
                            }
                        )
                    }

                    // Combined route with optional parameters
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

                    // Recipe detail
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

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                            onNavigateToLogin = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}