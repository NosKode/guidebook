package com.guidebook.app.data.local.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Копирует содержимое content:// URI во временный файл в cacheDir.
 * Возвращает null, если URI недоступен.
 */
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val ext  = context.contentResolver.getType(uri)
            ?.substringAfterLast('/')
            ?.let { ".$it" }
            ?: ".jpg"
        val file = File.createTempFile("upload_", ext, context.cacheDir)
        file.outputStream().use { out -> inputStream.use { it.copyTo(out) } }
        file
    } catch (e: Exception) {
        null
    }
}
