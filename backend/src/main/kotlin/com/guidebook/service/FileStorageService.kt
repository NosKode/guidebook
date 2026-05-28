package com.guidebook.service

import com.guidebook.domain.exception.BadRequestException
import io.ktor.http.*
import io.ktor.http.content.*
import java.io.File
import java.util.UUID

class FileStorageService(storagePath: String) {

    private val imagesDir  = File(storagePath, "images").also { it.mkdirs() }
    private val photosDir  = File(storagePath, "images/photos").also { it.mkdirs() }
    private val avatarsDir = File(storagePath, "images/avatars").also { it.mkdirs() }

    private val maxSizeBytes = 10 * 1024 * 1024  // 10 MB

    fun saveAvatar(userId: UUID, part: PartData.FileItem): String {
        val ext = validateAndGetExtension(part)
        val bytes = readLimited(part)
        val filename = "$userId.$ext"
        File(avatarsDir, filename).writeBytes(bytes)
        return "avatars/$filename"
    }

    fun saveCover(placeId: UUID, part: PartData.FileItem): String {
        val ext = validateAndGetExtension(part)
        val bytes = readLimited(part)
        val filename = "$placeId.$ext"
        File(imagesDir, filename).writeBytes(bytes)
        return filename
    }

    fun savePhoto(photoId: UUID, part: PartData.FileItem): String {
        val ext = validateAndGetExtension(part)
        val bytes = readLimited(part)
        val filename = "$photoId.$ext"
        File(photosDir, filename).writeBytes(bytes)
        return "photos/$filename"
    }

    fun deleteFile(relativePath: String): Boolean {
        val target = File(imagesDir, relativePath).canonicalFile
        if (!target.absolutePath.startsWith(imagesDir.canonicalPath)) return false
        return target.delete()
    }

    private fun validateAndGetExtension(part: PartData.FileItem): String {
        val contentType = part.contentType
            ?: throw BadRequestException("Missing content type for uploaded file")
        return when {
            contentType.match(ContentType.Image.JPEG)          -> "jpg"
            contentType.match(ContentType.Image.PNG)           -> "png"
            contentType.match(ContentType.parse("image/webp")) -> "webp"
            else -> throw BadRequestException("Unsupported image type '$contentType'. Allowed: jpeg, png, webp")
        }
    }

    private fun readLimited(part: PartData.FileItem): ByteArray {
        val bytes = part.streamProvider().readNBytes(maxSizeBytes + 1)
        if (bytes.size > maxSizeBytes)
            throw BadRequestException("File too large (max 10 MB)")
        if (bytes.isEmpty())
            throw BadRequestException("Empty file")
        return bytes
    }
}
