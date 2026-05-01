package com.example.yuntushaomiaojia.data.file

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileStorageRepository(private val context: Context) {

    @Throws(IOException::class)
    fun loadBitmap(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("无法读取图片")
        val bitmap = inputStream.use { stream -> BitmapFactory.decodeStream(stream) } ?: throw IOException("图片解码失败")
        return rotateBitmapByExif(bitmap, uri)
    }

    @Throws(IOException::class)
    fun loadBitmap(file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: throw IOException("图片解码失败")
        return rotateBitmapByExif(bitmap, file)
    }

    @Throws(IOException::class)
    fun loadCapturedCameraBitmap(uri: Uri, fallbackFile: File?): Bitmap {
        if (fallbackFile != null && fallbackFile.exists() && fallbackFile.length() > 0L) {
            return loadBitmap(fallbackFile)
        }
        return loadBitmap(uri)
    }

    fun scaleInside(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val scale = minOf(maxWidth / source.width.toFloat(), maxHeight / source.height.toFloat())
        val width = maxOf(1, Math.round(source.width * scale))
        val height = maxOf(1, Math.round(source.height * scale))
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    fun scaleDownIfNeeded(source: Bitmap, maxSide: Int): Bitmap {
        val largestSide = maxOf(source.width, source.height)
        if (largestSide <= maxSide) {
            return source
        }
        val scale = maxSide / largestSide.toFloat()
        val width = maxOf(1, Math.round(source.width * scale))
        val height = maxOf(1, Math.round(source.height * scale))
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    fun centerCropSquare(source: Bitmap, size: Int): Bitmap {
        val side = minOf(source.width, source.height)
        val left = (source.width - side) / 2
        val top = (source.height - side) / 2
        val square = Bitmap.createBitmap(source, left, top, side, side)
        return Bitmap.createScaledBitmap(square, size, size, true)
    }

    @Throws(IOException::class)
    fun saveBitmap(
        bitmap: Bitmap,
        folder: String,
        prefix: String,
        format: Bitmap.CompressFormat,
        quality: Int
    ): File {
        val extension = if (format == Bitmap.CompressFormat.PNG) ".png" else ".jpg"
        val displayName = "${prefix}_${timeText()}"
        val file = File(ensureOutputDir(folder), displayName + extension)
        writeBitmap(bitmap, file, format, quality)
        saveBitmapToGallery(bitmap, displayName, format, quality)
        return file
    }

    @Throws(IOException::class)
    fun writeBitmap(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat, quality: Int) {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(format, quality, outputStream)
            outputStream.flush()
        }
    }

    @Throws(IOException::class)
    fun saveBitmapToGallery(
        bitmap: Bitmap,
        displayName: String,
        format: Bitmap.CompressFormat,
        quality: Int
    ): Uri {
        val extension = if (format == Bitmap.CompressFormat.PNG) ".png" else ".jpg"
        val mimeType = if (format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + extension)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/全能工具箱")
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("相册写入失败")
        val outputStream = context.contentResolver.openOutputStream(uri) ?: throw IOException("相册文件打开失败")
        outputStream.use { stream ->
            bitmap.compress(format, quality, stream)
            stream.flush()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
        }
        return uri
    }

    fun ensureOutputDir(folder: String): File {
        val base = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val dir = File(base, folder)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    @Throws(IOException::class)
    fun readAllBytes(uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("无法读取文件")
        val outputStream = ByteArrayOutputStream()
        inputStream.use { stream ->
            val buffer = ByteArray(BUFFER_SIZE)
            var length = stream.read(buffer)
            while (length != -1) {
                outputStream.write(buffer, 0, length)
                length = stream.read(buffer)
            }
        }
        return outputStream.toByteArray()
    }

    fun timeText(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
    }

    private fun rotateBitmapByExif(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            inputStream.use { stream -> rotateBitmapByExif(bitmap, ExifInterface(stream)) }
        } catch (e: IOException) {
            bitmap
        }
    }

    private fun rotateBitmapByExif(bitmap: Bitmap, file: File): Bitmap {
        return try {
            rotateBitmapByExif(bitmap, ExifInterface(file.absolutePath))
        } catch (e: IOException) {
            bitmap
        }
    }

    private fun rotateBitmapByExif(bitmap: Bitmap, exifInterface: ExifInterface): Bitmap {
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.postRotate(90f)
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.postRotate(180f)
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            matrix.postRotate(270f)
        }

        if (matrix.isIdentity) {
            return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        private const val BUFFER_SIZE = 8192
    }
}
