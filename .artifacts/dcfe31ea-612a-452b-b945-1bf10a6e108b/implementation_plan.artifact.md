# Fix Firebase Dependency Resolution Error

The project is failing to sync because it tries to resolve `com.google.firebase:firebase-core-ktx:32.5.0`. `firebase-core` is a deprecated library, and its versioning does not align with the version number provided (`32.5.0`). Modern Firebase projects should use the Firebase Bill of Materials (BoM) and specific service libraries like `firebase-analytics`.

## User Review Required

> [!IMPORTANT]
> I am removing `firebase-core` as it is deprecated. If you intended to use Firebase Analytics, it is the recommended replacement. I will also standardize the use of the Firebase BoM for other Firebase libraries to ensure version compatibility.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Windows/StudioProjects/CpE3323_Capstone_Cookbook/gradle/libs.versions.toml)
- Remove `firebase-core` definition.
- Remove `firebase` version entry.
- Update `firebase-storage` to use the group `com.google.firebase` and name `firebase-storage` (dropping `-ktx` as it's merged into the main artifact in newer versions) and remove the version reference to rely on the BoM.
- Add `firebase-analytics` to libraries.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Windows/StudioProjects/CpE3323_Capstone_Cookbook/app/build.gradle.kts)
- Remove `implementation(libs.firebase.core)`.
- Remove the hardcoded `implementation("com.google.firebase:firebase-storage-ktx:20.3.0")`.
- Add `implementation(libs.firebase.storage)` using the catalog version.
- (Optional) Add `implementation(libs.firebase.analytics)` if analytics is desired.

## Verification Plan

### Automated Tests
- Run Gradle Sync to ensure all dependencies resolve correctly.
- Execute `./gradlew :app:assembleDebug` to verify the build.

### Manual Verification
- Verify that the IDE no longer shows resolution errors for Firebase libraries.
