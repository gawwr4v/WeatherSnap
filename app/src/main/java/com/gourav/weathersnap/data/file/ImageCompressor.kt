package com.gourav.weathersnap.data.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// this class handles reducing the file size of photos taken by the user
@Singleton
class ImageCompressor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    // we compress the image on a background thread to keep the app smooth
    suspend fun compress(originalPath: String): CompressedImage = withContext(Dispatchers.IO) {
        val originalFile = File(originalPath)
        // load the original image from the disk into memory
        val bitmap = BitmapFactory.decodeFile(originalPath)
            ?: error("Could not read captured image.")

        // we create a specific folder for our compressed reports if it doesnt exist
        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val compressedFile = File(reportsDir, "compressed_${System.currentTimeMillis()}.jpg")

        // we save the image as a jpeg with a specific quality level to save space
        // the saved report keeps the compressed copy, the original temp file is removed after save
        FileOutputStream(compressedFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        // free up memory as soon as we are done with the bitmap
        bitmap.recycle()

        // return all the info about the compressed file and its original size
        CompressedImage(
            originalPath = originalFile.absolutePath,
            compressedPath = compressedFile.absolutePath,
            originalSizeBytes = originalFile.length(),
            compressedSizeBytes = compressedFile.length(),
        )
    }

    companion object {
        // 65 is a good balance between image quality and small file size
        private const val JPEG_QUALITY = 65
    }
}
