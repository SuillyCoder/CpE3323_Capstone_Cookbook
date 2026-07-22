# Implementation Plan - Fix Unresolved Reference 'RecipeDetailScreen'

The build error `Unresolved reference 'RecipeDetailScreen'` is caused by a package mismatch. `RecipeDetailScreen.kt` declares its package as `com.example.cpe3323_capstone_cookbook.ui.recipe`, but `MainActivity.kt` tries to import it from `com.example.cpe3323_capstone_cookbook.ui`. Additionally, the file is physically located in the `ui` directory instead of the `ui/recipe` directory.

## Proposed Changes

### [app component]

#### [MODIFY] [MainActivity.kt](file:///E:/programming/CpE3323_Capstone_Cookbook/app/src/main/java/com/example/cpe3323_capstone_cookbook/MainActivity.kt)
- Update the import statement for `RecipeDetailScreen` to use the correct package: `com.example.cpe3323_capstone_cookbook.ui.recipe.RecipeDetailScreen`.

#### [MOVE] [RecipeDetailScreen.kt](file:///E:/programming/CpE3323_Capstone_Cookbook/app/src/main/java/com/example/cpe3323_capstone_cookbook/ui/RecipeDetailScreen.kt)
- Move `RecipeDetailScreen.kt` from `.../ui/` to `.../ui/recipe/` to match its package declaration.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the project builds successfully.
