package com.gity.feliyaattendance.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resumeWithException

class CloudinaryHelper(private val context: Context) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun uploadImage(imageUri: Uri, userId: String): String =
        suspendCancellableCoroutine { continuation ->
            try {

//            Kompress gambar
                val compressedFile = compressImage(imageUri)

//            Struktur Folder: Home/userId/attendance
                val folder = "Home/$userId/attendance"
                val fileName = "attendance_${System.currentTimeMillis()}"

                MediaManager.get().upload(compressedFile.absolutePath)
                    .option("folder", folder)
                    .option("public_id", fileName)
                    .callback(object : UploadCallback {
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as String
                            continuation.resume(imageUrl) {
                                compressedFile.delete() // Hapus file setelah selesai
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            continuation.resumeWithException(Exception(error.description))
                            compressedFile.delete()
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            // Tambahkan kode untuk menangani progress jika diperlukan
                        }

                        override fun onStart(requestId: String) {}
                        override fun onReschedule(requestId: String, error: ErrorInfo) {}

                    })
                    .dispatch()

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    private fun compressImage(imageUri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Kompres bitmap ke JPEG
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

        val tempFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use {
            it.write(outputStream.toByteArray())
        }

        return tempFile
    }
}