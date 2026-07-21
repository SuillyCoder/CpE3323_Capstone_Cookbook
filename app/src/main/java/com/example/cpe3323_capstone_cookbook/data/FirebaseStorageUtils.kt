package com.example.cpe3323_capstone_cookbook.data


import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageUtils(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseStorageUtils"
    }

    suspend fun uploadRecipeImage(authorId: String, imageUri: Uri): Result<String> {
        return try {
            Log.d(TAG, "=== STARTING UPLOAD ===")
            Log.d(TAG, "Author ID: $authorId")
            Log.d(TAG, "Image URI: $imageUri")

            // Check authentication
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "❌ No user is signed in!")
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "✅ User authenticated: ${currentUser.uid}")

            if (currentUser.uid != authorId) {
                Log.e(TAG, "❌ User ID mismatch! Current: ${currentUser.uid}, Expected: $authorId")
                return Result.failure(Exception("User ID mismatch"))
            }

            // Create file path
            val fileName = "recipes/$authorId/${UUID.randomUUID()}.jpg"
            Log.d(TAG, "📁 File path: $fileName")

            val storageRef = storage.reference.child(fileName)
            Log.d(TAG, "📤 Starting upload...")

            // Upload the file
            val uploadTask = storageRef.putFile(imageUri)
            val snapshot = uploadTask.await()

            Log.d(TAG, "✅ Upload completed! Bytes transferred: ${snapshot.bytesTransferred}")

            // Get download URL
            Log.d(TAG, "🔗 Getting download URL...")
            val downloadUrl = storageRef.downloadUrl.await()
            val urlString = downloadUrl.toString()

            Log.d(TAG, "✅ Download URL: $urlString")
            Log.d(TAG, "=== UPLOAD SUCCESSFUL ===")

            Result.success(urlString)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Upload failed!", e)
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Error cause: ${e.cause}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteRecipeImage(imageUrl: String): Result<Unit> {
        return try {
            if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageRef.delete().await()
                Log.d(TAG, "Deleted image: $imageUrl")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed", e)
            Result.success(Unit)
        }
    }
}