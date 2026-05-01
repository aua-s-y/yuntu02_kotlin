package com.example.yuntushaomiaojia.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.yuntushaomiaojia.data.bookkeeping.BookkeepingRepository
import com.example.yuntushaomiaojia.data.conversion.ConversionRepository
import com.example.yuntushaomiaojia.data.crypto.FileCryptoRepository
import com.example.yuntushaomiaojia.data.file.FileStorageRepository
import com.example.yuntushaomiaojia.data.image.ImageProcessResult
import com.example.yuntushaomiaojia.data.image.ImageProcessingRepository
import com.example.yuntushaomiaojia.data.pdf.PdfExportResult
import com.example.yuntushaomiaojia.data.pdf.PdfRepository
import com.example.yuntushaomiaojia.data.recognition.ImageRecognitionRepository
import com.example.yuntushaomiaojia.data.recognition.TextRecognitionRepository
import com.example.yuntushaomiaojia.data.tool.ToolRepository
import com.example.yuntushaomiaojia.data.travel.TravelListRepository
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ToolViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val toolRepository = ToolRepository()
    private val conversionRepository = ConversionRepository()
    private val imageRecognitionRepository = ImageRecognitionRepository()
    private val textRecognitionRepository = TextRecognitionRepository()
    private val fileStorageRepository = FileStorageRepository(appContext)
    private val pdfRepository = PdfRepository(appContext, fileStorageRepository)
    private val fileCryptoRepository = FileCryptoRepository(fileStorageRepository)
    private val imageProcessingRepository = ImageProcessingRepository(fileStorageRepository)
    private val travelListRepository = TravelListRepository(appContext)
    private val bookkeepingRepository = BookkeepingRepository(appContext)

    fun titleText(toolId: String): String {
        return toolRepository.getTitleText(appContext, toolId)
    }

    fun descriptionText(toolId: String): String {
        return toolRepository.getDescriptionText(appContext, toolId)
    }

    fun cameraFolderName(toolId: String?): String {
        return toolRepository.cameraFolderName(toolId.orEmpty())
    }

    fun supportsCameraRecognition(toolId: String): Boolean {
        return toolRepository.supportsCameraRecognition(toolId)
    }

    fun isImageLabelingTool(toolId: String?): Boolean {
        return toolRepository.isImageLabelingTool(toolId)
    }

    fun createCameraFile(action: String?): File {
        return File(fileStorageRepository.ensureOutputDir(cameraFolderName(action)), "camera_${fileStorageRepository.timeText()}.jpg")
    }

    fun loadCapturedCameraBitmap(uri: Uri, file: File?): Bitmap {
        return fileStorageRepository.loadCapturedCameraBitmap(uri, file)
    }

    fun loadBitmap(uri: Uri): Bitmap {
        return fileStorageRepository.loadBitmap(uri)
    }

    fun loadBitmap(file: File): Bitmap {
        return fileStorageRepository.loadBitmap(file)
    }

    fun scaleDownIfNeeded(bitmap: Bitmap, maxSide: Int): Bitmap {
        return fileStorageRepository.scaleDownIfNeeded(bitmap, maxSide)
    }

    fun saveScanBitmap(bitmap: Bitmap): Unit {
        fileStorageRepository.saveBitmapToGallery(bitmap, "scan_${fileStorageRepository.timeText()}", Bitmap.CompressFormat.JPEG, 92)
    }

    fun exportPdfToImages(uri: Uri): PdfExportResult {
        return pdfRepository.exportPagesToImages(uri)
    }

    fun createPdfFromImages(uris: List<Uri>): File {
        return pdfRepository.createPdfFromImages(uris)
    }

    fun compressPdf(uri: Uri): File {
        return pdfRepository.compressPdf(uri)
    }

    fun encryptFile(uri: Uri, password: String): File {
        return fileCryptoRepository.encryptFile(uri, password)
    }

    fun decryptFile(uri: Uri, password: String): File {
        return fileCryptoRepository.decryptFile(uri, password)
    }

    fun addWatermark(bitmap: Bitmap, text: String): ImageProcessResult {
        return imageProcessingRepository.addWatermark(bitmap, text)
    }

    fun createPixelArt(bitmap: Bitmap): ImageProcessResult {
        return imageProcessingRepository.createPixelArt(bitmap)
    }

    fun colorizeImage(bitmap: Bitmap): ImageProcessResult {
        return imageProcessingRepository.colorizeImage(bitmap)
    }

    fun createPhotoGrid(uris: List<Uri>): ImageProcessResult {
        return imageProcessingRepository.createPhotoGrid(uris)
    }

    fun recognizeTextFromUri(
        uri: Uri,
        onPreview: (Bitmap) -> Unit,
        onResult: (String) -> Unit
    ) {
        val previewBitmap = scaleDownIfNeeded(loadBitmap(uri), 1600)
        onPreview(previewBitmap)
        textRecognitionRepository.recognizeText(
            InputImage.fromFilePath(appContext, uri),
            onSuccess = onResult,
            onFailure = onResult
        )
    }

    fun recognizeTextFromBitmap(
        bitmap: Bitmap,
        onPreview: (Bitmap) -> Unit,
        onResult: (String) -> Unit
    ) {
        onPreview(bitmap)
        val image = InputImage.fromBitmap(scaleDownIfNeeded(bitmap, 1600), 0)
        textRecognitionRepository.recognizeText(image, onSuccess = onResult, onFailure = onResult)
    }

    fun recognizeImageFromUri(
        uri: Uri,
        action: String,
        onPreview: (Bitmap) -> Unit,
        onResult: (String) -> Unit
    ) {
        val bitmap = scaleDownIfNeeded(loadBitmap(uri), 900)
        recognizeImageFromBitmap(bitmap, action, onPreview, onResult)
    }

    fun recognizeImageFromBitmap(
        bitmap: Bitmap,
        action: String,
        onPreview: (Bitmap) -> Unit,
        onResult: (String) -> Unit
    ) {
        onPreview(bitmap)
        imageRecognitionRepository.recognizeImage(bitmap, action, onResult)
    }

    fun convertCurrency(amountText: String, fromCurrency: String, toCurrency: String): String {
        return conversionRepository.convertCurrencyText(amountText, fromCurrency, toCurrency)
    }

    fun convertBase(numberText: String, selectedBase: Int): String {
        return conversionRepository.convertBaseText(numberText, selectedBase)
    }

    fun addTravelItem(item: String) {
        travelListRepository.addItem(item)
    }

    fun clearTravelItems() {
        travelListRepository.clearItems()
    }

    fun travelItemsText(): String {
        return travelListRepository.getItemsText("清单还是空的，先添加一个物品。")
    }

    fun addExpense(amount: Double, category: String, note: String) {
        bookkeepingRepository.addExpense(amount, category, note, displayTimeText())
    }

    fun clearExpenses() {
        bookkeepingRepository.clearExpenses()
    }

    fun bookkeepingSummaryText(): String {
        return bookkeepingRepository.getSummaryText("合计：", "还没有记账记录。")
    }

    fun scanArchiveFiles(): Array<File> {
        val files = fileStorageRepository.ensureOutputDir(ToolRepository.SCAN_ARCHIVE).listFiles(File::isFile)
        return files.orEmpty().sortedByDescending { file -> file.lastModified() }.toTypedArray()
    }

    fun archiveTimeText(file: File): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(file.lastModified()))
    }

    fun imageMimeType(file: File): String {
        return if (file.name.lowercase(Locale.ROOT).endsWith(".png")) "image/png" else "image/jpeg"
    }

    private fun displayTimeText(): String {
        return SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date())
    }
}
