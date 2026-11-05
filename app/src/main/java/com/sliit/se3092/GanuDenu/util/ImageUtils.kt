package com.sliit.se3092.GanuDenu.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // 1. Save Gallery URI to Local Storage
    fun saveUriToLocal(context: Context, uri: Uri, userId: String): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            saveBitmapToLocal(context, originalBitmap, userId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 2. Save Camera Bitmap to Local Storage
    fun saveBitmapToLocal(context: Context, bitmap: Bitmap, userId: String): Bitmap? {
        return try {
            // Keep it light (400x400) for instant UI rendering
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
            val file = File(context.filesDir, "profile_$userId.jpg")
            val outputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            scaledBitmap // Return the bitmap so the UI updates instantly
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. Load Bitmap from Local Storage
    fun loadLocalProfileImage(context: Context, userId: String): Bitmap? {
        val file = File(context.filesDir, "profile_$userId.jpg")
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    // 4. Remove Image
    fun removeLocalProfileImage(context: Context, userId: String) {
        val file = File(context.filesDir, "profile_$userId.jpg")
        if (file.exists()) file.delete()
    }
}