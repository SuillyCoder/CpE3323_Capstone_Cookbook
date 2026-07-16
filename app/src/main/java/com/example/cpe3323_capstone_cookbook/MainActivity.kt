package com.example.cpe3323_capstone_cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cpe3323_capstone_cookbook.ui.auth.LoginScreen
import com.example.cpe3323_capstone_cookbook.ui.auth.RegisterScreen
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
                    composable("home") { HomePlaceholder() }
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

@Composable
fun HomePlaceholder() {
    Text("Home screen — logged in!")
}