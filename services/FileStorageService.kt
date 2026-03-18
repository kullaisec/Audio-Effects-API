package com.voiceai.services

import java.io.File
import java.util.UUID

class FileStorageService {
    private val uploadDirectory = "/var/voiceai/uploads"

    fun saveUploadedFile(bytes: ByteArray, originalName: String): String {
        val extension = originalName.substringAfterLast('.', "mp3")
        val fileName = "${UUID.randomUUID()}.$extension"
        val file = File("$uploadDirectory/$fileName")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }
}
