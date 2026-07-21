package com.example.cpe3323_capstone_cookbook


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType  // Added import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument  // Added import
import com.example.cpe3323_capstone_cookbook.ui.HomeScreen  // Added import
import com.example.cpe3323_capstone_cookbook.ui.auth.LoginScreen
import com.example.cpe3323_capstone_cookbook.ui.auth.RegisterScreen
import com.example.cpe3323_capstone_cookbook.ui.recipe.AddEditRecipeScreen  // Fixed import path
import com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeListScreen
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
                            onRecipeClick = { recipe -> navController.navigate("recipeForm/${recipe.authorId}/${recipe.id}") }
                        )
                    }
                    composable("recipeForm") {
                        AddEditRecipeScreen(recipeId = null,
                            authorId = null, onDone = { navController.popBackStack() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "recipeForm/{authorId}/{recipeId}",
                        arguments = listOf(
                            navArgument("authorId") { type = NavType.StringType },
                            navArgument("recipeId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        AddEditRecipeScreen(
                            recipeId = backStackEntry.arguments?.getString("recipeId"),
                            authorId = backStackEntry.arguments?.getString("authorId"),
                            onDone = { navController.popBackStack() },
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