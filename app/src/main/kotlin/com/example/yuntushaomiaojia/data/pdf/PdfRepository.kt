package com.example.yuntushaomiaojia.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.example.yuntushaomiaojia.data.file.FileStorageRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfRepository(
    private val context: Context,
    private val fileStorageRepository: FileStorageRepository
) {

    @Throws(IOException::class)
    fun exportPagesToImages(uri: Uri): PdfExportResult {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IOException("无法打开 PDF")
        descriptor.use { parcelFileDescriptor ->
            PdfRenderer(parcelFileDescriptor).use { renderer ->
                val outputDir = fileStorageRepository.ensureOutputDir("pdf_pages")
                val time = fileStorageRepository.timeText()
                val builder = StringBuilder("已导出图片：\n")
                var firstImageFile: File? = null

                for (index in 0 until renderer.pageCount) {
                    val page = renderer.openPage(index)
                    try {
                        val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val file = File(outputDir, "pdf_${time}_page_${index + 1}.png")
                        fileStorageRepository.writeBitmap(bitmap, file, Bitmap.CompressFormat.PNG, 100)
                        fileStorageRepository.saveBitmapToGallery(bitmap, "pdf_${time}_page_${index + 1}", Bitmap.CompressFormat.PNG, 100)
                        if (index == 0) {
                            firstImageFile = file
                        }
                        builder.append(file.absolutePath).append("\n")
                    } finally {
                        page.close()
                    }
                }
                return PdfExportResult(builder.append("\n已同步写入系统相册。").toString(), firstImageFile)
            }
        }
    }

    @Throws(IOException::class)
    fun createPdfFromImages(uris: List<Uri>): File {
        val document = PdfDocument()
        return try {
            val pageWidth = 595
            val pageHeight = 842
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            uris.forEachIndexed { index, uri ->
                val bitmap = fileStorageRepository.loadBitmap(uri)
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)
                val scaled = fileStorageRepository.scaleInside(bitmap, pageWidth - 40, pageHeight - 40)
                val left = (pageWidth - scaled.width) / 2f
                val top = (pageHeight - scaled.height) / 2f
                canvas.drawBitmap(scaled, left, top, paint)
                document.finishPage(page)
            }

            val file = File(fileStorageRepository.ensureOutputDir("pdf"), "images_${fileStorageRepository.timeText()}.pdf")
            FileOutputStream(file).use { outputStream -> document.writeTo(outputStream) }
            file
        } finally {
            document.close()
        }
    }

    @Throws(IOException::class)
    fun compressPdf(uri: Uri): File {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: throw IOException("无法打开 PDF")
        descriptor.use { parcelFileDescriptor ->
            PdfRenderer(parcelFileDescriptor).use { renderer ->
                val document = PdfDocument()
                return try {
                    val pageWidth = 595
                    val pageHeight = 842
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    for (index in 0 until renderer.pageCount) {
                        val sourcePage = renderer.openPage(index)
                        try {
                            val bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.RGB_565)
                            val bitmapCanvas = Canvas(bitmap)
                            bitmapCanvas.drawColor(Color.WHITE)
                            sourcePage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                            val targetPage = document.startPage(pageInfo)
                            targetPage.canvas.drawBitmap(bitmap, 0f, 0f, paint)
                            document.finishPage(targetPage)
                        } finally {
                            sourcePage.close()
                        }
                    }

                    val file = File(fileStorageRepository.ensureOutputDir("pdf"), "compressed_${fileStorageRepository.timeText()}.pdf")
                    FileOutputStream(file).use { outputStream -> document.writeTo(outputStream) }
                    file
                } finally {
                    document.close()
                }
            }
        }
    }
}
