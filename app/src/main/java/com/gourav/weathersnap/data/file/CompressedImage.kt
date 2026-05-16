package com.gourav.weathersnap.data.file

// this class acts as a data holder for image compression results
data class CompressedImage(
    // where the full size image is located
    val originalPath: String,
    // where the smaller version is stored
    val compressedPath: String,
    // size of the original file in bytes
    val originalSizeBytes: Long,
    // size of the compressed file in bytes
    val compressedSizeBytes: Long,
)
