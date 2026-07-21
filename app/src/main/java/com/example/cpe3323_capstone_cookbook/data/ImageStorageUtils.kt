package com.example.cpe3323_capstone_cookbook.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val fileName = "recipe_${UUID.randomUUID()}.jpg"
        val outputFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output -> input.copyTo(output) }
        }
        outputFile.absolutePath
    } catch (e: IOException) {
        null
    }
}