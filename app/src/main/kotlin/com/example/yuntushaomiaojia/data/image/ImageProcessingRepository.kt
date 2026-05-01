package com.example.yuntushaomiaojia.data.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import com.example.yuntushaomiaojia.data.file.FileStorageRepository
import java.io.IOException

class ImageProcessingRepository(private val fileStorageRepository: FileStorageRepository) {

    @Throws(IOException::class)
    fun addWatermark(source: Bitmap, text: String): ImageProcessResult {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.argb(210, 255, 255, 255)
        paint.textSize = maxOf(32f, result.width / 18f)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(6f, 2f, 2f, Color.argb(160, 0, 0, 0))
        val x = result.width - paint.measureText(text) - 32
        val y = result.height - 40f
        canvas.drawText(text, maxOf(24f, x), y, paint)

        val file = fileStorageRepository.saveBitmap(result, "watermark", "watermark", Bitmap.CompressFormat.JPEG, 92)
        return ImageProcessResult(result, file, "水印图片已保存", "image/jpeg", "打开图片")
    }

    @Throws(IOException::class)
    fun createPhotoGrid(uris: List<Uri>): ImageProcessResult {
        val cellSize = 360
        val gap = 8
        val outputSize = cellSize * 3 + gap * 2
        val result = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        for (index in 0 until PHOTO_GRID_IMAGE_COUNT) {
            val bitmap = fileStorageRepository.centerCropSquare(
                fileStorageRepository.scaleDownIfNeeded(fileStorageRepository.loadBitmap(uris[index]), 1200),
                cellSize
            )
            val row = index / 3
            val col = index % 3
            val left = col * (cellSize + gap)
            val top = row * (cellSize + gap)
            canvas.drawBitmap(bitmap, left.toFloat(), top.toFloat(), paint)
        }

        val file = fileStorageRepository.saveBitmap(result, "photo_grid", "grid", Bitmap.CompressFormat.JPEG, 92)
        return ImageProcessResult(result, file, "九宫格图片已生成", "image/jpeg", "打开图片")
    }

    @Throws(IOException::class)
    fun createPixelArt(source: Bitmap): ImageProcessResult {
        val safeSource = fileStorageRepository.scaleDownIfNeeded(source, 1200)
        val small = Bitmap.createScaledBitmap(safeSource, 48, 48, false)
        val result = Bitmap.createScaledBitmap(small, safeSource.width, safeSource.height, false)
        val file = fileStorageRepository.saveBitmap(result, "pixel_art", "pixel", Bitmap.CompressFormat.PNG, 100)
        return ImageProcessResult(result, file, "像素图已保存", "image/png", "打开图片")
    }

    @Throws(IOException::class)
    fun colorizeImage(source: Bitmap): ImageProcessResult {
        val safeSource = fileStorageRepository.scaleDownIfNeeded(source, 1200).copy(Bitmap.Config.ARGB_8888, false)
        val width = safeSource.width
        val height = safeSource.height
        val pixels = IntArray(width * height)
        safeSource.getPixels(pixels, 0, width, 0, 0, width, height)

        for (index in pixels.indices) {
            val color = pixels[index]
            val gray = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
            val red = minOf(255, gray + 28)
            val green = minOf(255, (gray * 0.92f).toInt() + 45)
            val blue = minOf(255, (gray * 0.68f).toInt() + 82)
            pixels[index] = Color.argb(Color.alpha(color), red, green, blue)
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        val file = fileStorageRepository.saveBitmap(result, "colorize", "colorize", Bitmap.CompressFormat.JPEG, 92)
        return ImageProcessResult(result, file, "上色图片已保存", "image/jpeg", "打开图片")
    }

    private companion object {
        private const val PHOTO_GRID_IMAGE_COUNT = 9
    }
}
