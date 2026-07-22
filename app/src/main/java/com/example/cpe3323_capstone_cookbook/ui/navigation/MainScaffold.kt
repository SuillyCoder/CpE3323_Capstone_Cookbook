package com.example.cpe3323_capstone_cookbook.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cpe3323_capstone_cookbook.data.Recipe
import com.example.cpe3323_capstone_cookbook.ui.HomeScreen
import com.example.cpe3323_capstone_cookbook.ui.explore.ExploreScreen
import com.example.cpe3323_capstone_cookbook.ui.profile.ProfileScreen
import com.example.cpe3323_capstone_cookbook.ui.saved.SavedScreen
import com.example.cpe3323_capstone_cookbook.ui.theme.BurntOrange
import com.example.cpe3323_capstone_cookbook.ui.theme.TextSecondary

enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    Explore("explore", "Explore", Icons.Filled.Search, Icons.Outlined.Search),
    Saved("saved", "Saved", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    Profile("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScaffold(
    onAddClick: () -> Unit,
    onEditClick: (Recipe) -> Unit,
    onViewClick: (Recipe) -> Unit,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurntOrange,
                            selectedTextColor = BurntOrange,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BurntOrange.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onAddClick = onAddClick,
                    onEditClick = onEditClick,
                    onViewClick = onViewClick
                )
            }
            composable(BottomNavItem.Explore.route) {
                ExploreScreen(
                    onRecipeClick = onViewClick,
                    onAddClick = onAddClick
                )
            }
            composable(BottomNavItem.Saved.route) {
                SavedScreen(onRecipeClick = onViewClick)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onAddClick = onAddClick,
                    onRecipeClick = { authorId, recipeId ->
                        onViewClick(
                            Recipe(id = recipeId, authorId = authorId)
                        )
                    }
                )
            }
        }
    }
}
