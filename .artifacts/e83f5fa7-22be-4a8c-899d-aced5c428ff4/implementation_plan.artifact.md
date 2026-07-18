# Plan: Connect Recipe List Feature to App Navigation

The user has implemented a `RecipeListScreen` and associated data classes/repositories, but the feature is not visible in the emulator because it hasn't been added to the `MainActivity` navigation graph. Currently, the "home" destination shows a placeholder text.

## User Review Required

> [!IMPORTANT]
> I will be updating `MainActivity.kt` to replace the "Home" placeholder with your actual `RecipeListScreen`.
> I also suggest temporarily setting `RecipeListScreen` as the starting screen if you want to test it immediately without logging in.

## Proposed Changes

### [UI / Navigation]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Windows/StudioProjects/CpE3323_Capstone_Cookbook/app/src/main/java/com/example/cpe3323_capstone_cookbook/MainActivity.kt)
- Import `RecipeListScreen`.
- Replace `HomePlaceholder()` with `RecipeListScreen()` in the `NavHost`.
- (Optional) Update `startDestination` to `"home"` for faster testing of the new feature.

## Verification Plan

### Manual Verification
- Deploy the app to the emulator.
- Verify that after logging in (or immediately, if `startDestination` is changed), the "Recipes" screen appears.
- Test the "Add" button (+) to ensure it adds a test recipe to Firestore.
