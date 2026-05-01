package com.example.yuntushaomiaojia.ui.tool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.yuntushaomiaojia.R
import com.example.yuntushaomiaojia.data.image.ImageProcessResult
import com.example.yuntushaomiaojia.data.pdf.PdfExportResult
import com.example.yuntushaomiaojia.data.tool.ToolRepository
import com.example.yuntushaomiaojia.databinding.ActivityToolBinding
import com.example.yuntushaomiaojia.ui.tool.widget.CompassDialView
import com.example.yuntushaomiaojia.viewmodel.ToolViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

class ToolActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityToolBinding
    private lateinit var viewModel: ToolViewModel

    private var toolId = ToolRepository.SCAN_ARCHIVE
    private var pendingImageAction: String? = null
    private var pendingCameraAction: String? = null
    private var pendingPdfAction: String? = null
    private var pendingFileAction: String? = null
    private var pendingMultipleImageAction: String? = null
    private var pendingCameraUri: Uri? = null
    private var pendingCameraFile: File? = null

    private var resultView: TextView? = null
    private var secondaryResultView: TextView? = null
    private var accessButton: Button? = null
    private val baseOptionViews = mutableListOf<TextView>()
    private var selectedBase = 10
    private var compassView: TextView? = null
    private var compassDialView: CompassDialView? = null
    private var travelListView: TextView? = null
    private var bookkeepingListView: TextView? = null
    private var fontPreviewView: TextView? = null
    private var imagePreview: ImageView? = null
    private var watermarkInput: EditText? = null
    private var passwordInput: EditText? = null
    private var travelInput: EditText? = null
    private var expenseAmountInput: EditText? = null
    private var expenseNoteInput: EditText? = null
    private var fontInput: EditText? = null

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null
    private val gravityValues = FloatArray(3)
    private val magneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasMagnetic = false
    private var compassActive = false

    private val cameraPictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val cameraUri = pendingCameraUri
            if (!success || cameraUri == null) {
                showToast("没有拍到照片")
                return@registerForActivityResult
            }
            handleCameraImage(cameraUri)
        }

    private val cameraPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraForCurrentAction()
            } else {
                showToast("需要相机权限才能拍照")
            }
        }

    private val imagePicker: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                showToast("没有选择图片")
                return@registerForActivityResult
            }
            handleImagePicked(uri)
        }

    private val multipleImagePicker: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            handleImagesPicked(uris)
        }

    private val pdfPicker: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                showToast("没有选择 PDF")
                return@registerForActivityResult
            }
            handlePdfPicked(uri)
        }

    private val filePicker: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                showToast("没有选择文件")
                return@registerForActivityResult
            }
            handleFilePicked(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ToolViewModel::class.java]
        toolId = intent.getStringExtra(EXTRA_TOOL_ID).takeUnless { value -> value.isNullOrEmpty() } ?: ToolRepository.SCAN_ARCHIVE

        binding.btnBack.setOnClickListener { finish() }
        binding.tvToolTitle.text = viewModel.titleText(toolId)
        renderTool()
    }

    private fun renderTool() {
        compassActive = false
        accessButton = null
        binding.toolContent.removeAllViews()
        addDescription(viewModel.descriptionText(toolId))

        when (toolId) {
            ToolRepository.SCAN_ARCHIVE -> renderScanArchiveTool()
            ToolRepository.TEXT_RECOGNITION -> renderImagePickTool("选择图片识别文字", ToolRepository.TEXT_RECOGNITION)
            ToolRepository.PLANT_RECOGNITION,
            ToolRepository.FRUIT_RECOGNITION,
            ToolRepository.ANIMAL_RECOGNITION -> renderImagePickTool("选择图片进行识别", toolId)
            ToolRepository.PDF_TO_IMAGE -> renderPdfPickTool("选择 PDF 转成图片", ToolRepository.PDF_TO_IMAGE)
            ToolRepository.IMAGE_TO_PDF -> renderImageToPdfTool()
            ToolRepository.ENCRYPT_PDF -> renderEncryptPdfTool()
            ToolRepository.COMPRESS_PDF -> renderPdfPickTool("选择 PDF 并压缩", ToolRepository.COMPRESS_PDF)
            ToolRepository.COMPASS -> renderCompassTool()
            ToolRepository.EXCHANGE_RATE -> renderExchangeRateTool()
            ToolRepository.WATERMARK -> renderWatermarkTool()
            ToolRepository.PHOTO_GRID -> renderPhotoGridTool()
            ToolRepository.PIXEL_ART -> renderImagePickTool("选择图片生成像素图", ToolRepository.PIXEL_ART)
            ToolRepository.COLORIZE -> renderImagePickTool("选择黑白图片进行上色", ToolRepository.COLORIZE)
            ToolRepository.TRAVEL_LIST -> renderTravelListTool()
            ToolRepository.FONT_ZOOM -> renderFontZoomTool()
            ToolRepository.BOOKKEEPING -> renderBookkeepingTool()
            ToolRepository.BASE_CONVERTER -> renderBaseConverterTool()
            else -> addText("这个工具还没有配置页面。", 16f, Typeface.NORMAL)
        }
    }

    private fun renderScanArchiveTool() {
        imagePreview = addImagePreview()
        resultView = addResultView()
        addButton("拍照并存档") { requestCameraAndTakePicture(ToolRepository.SCAN_ARCHIVE) }
        addButton("查看存档记录") { showScanArchiveDialog() }
    }

    private fun renderImagePickTool(buttonText: String, action: String) {
        imagePreview = addImagePreview()
        resultView = addResultView()
        addButton(buttonText) {
            pendingImageAction = action
            imagePicker.launch("image/*")
        }
        if (viewModel.supportsCameraRecognition(action)) {
            val cameraButtonText = if (action == ToolRepository.TEXT_RECOGNITION) "拍照识别文字" else "拍照识别"
            addButton(cameraButtonText) { requestCameraAndTakePicture(action) }
        }
    }

    private fun renderPdfPickTool(buttonText: String, action: String) {
        imagePreview = addImagePreview()
        resultView = addResultView()
        addButton(buttonText) {
            pendingPdfAction = action
            pdfPicker.launch("application/pdf")
        }
    }

    private fun renderImageToPdfTool() {
        resultView = addResultView()
        addButton("选择多张图片并生成 PDF") {
            pendingMultipleImageAction = ToolRepository.IMAGE_TO_PDF
            multipleImagePicker.launch("image/*")
        }
    }

    private fun renderPhotoGridTool() {
        imagePreview = addImagePreview()
        resultView = addResultView()
        addButton("选择 9 张图片合成九宫格") {
            pendingMultipleImageAction = ToolRepository.PHOTO_GRID
            multipleImagePicker.launch("image/*")
        }
    }

    private fun renderEncryptPdfTool() {
        passwordInput = addInput("请输入加密/解密密码", 1)
        resultView = addResultView()
        addButton("选择 PDF 并加密") {
            if (!hasPassword()) return@addButton
            pendingFileAction = ACTION_ENCRYPT_PDF
            filePicker.launch("application/pdf")
        }
        addButton("选择加密文件并解密") {
            if (!hasPassword()) return@addButton
            pendingFileAction = ACTION_DECRYPT_PDF
            filePicker.launch("*/*")
        }
    }

    private fun renderCompassTool() {
        compassDialView = CompassDialView(this)
        binding.toolContent.addView(compassDialView, compassLayoutParams())
        compassView = addText("正在读取方向……", 30f, Typeface.BOLD)
        compassView?.gravity = Gravity.CENTER
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticField = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        compassActive = true
        startCompass()
    }

    private fun renderExchangeRateTool() {
        val amountInput = addInput("输入金额，例如 100", 1)
        val fromSpinner = addSpinner(arrayOf("CNY 人民币", "USD 美元", "EUR 欧元", "JPY 日元", "KRW 韩元", "GBP 英镑"))
        val toSpinner = addSpinner(arrayOf("USD 美元", "CNY 人民币", "EUR 欧元", "JPY 日元", "KRW 韩元", "GBP 英镑"))
        resultView = addResultView()
        addButton("换算") { convertCurrency(amountInput, fromSpinner, toSpinner) }
    }

    private fun renderWatermarkTool() {
        watermarkInput = addInput("输入水印文字", 1)
        imagePreview = addImagePreview()
        resultView = addResultView()
        addButton("选择图片并添加水印") {
            if (watermarkInput?.text.toString().trim().isEmpty()) {
                showToast("请先输入水印文字")
                return@addButton
            }
            pendingImageAction = ToolRepository.WATERMARK
            imagePicker.launch("image/*")
        }
    }

    private fun renderTravelListTool() {
        travelInput = addInput("输入旅行物品，例如 身份证", 1)
        travelListView = addResultView()
        refreshTravelList()
        addButton("添加到清单") { addTravelItem() }
        addButton("清空清单") {
            viewModel.clearTravelItems()
            refreshTravelList()
        }
    }

    private fun renderFontZoomTool() {
        fontInput = addInput("输入要预览的文字", 2)
        fontInput?.setText("这是一段字体放大预览文字")
        val seekBar = SeekBar(this)
        seekBar.max = 60
        seekBar.progress = 20
        binding.toolContent.addView(seekBar, matchWrapParams())
        fontPreviewView = addText(fontInput?.text.toString(), 24f, Typeface.NORMAL)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fontPreviewView?.textSize = max(12, progress).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        addButton("刷新预览文字") { fontPreviewView?.text = fontInput?.text.toString() }
    }

    private fun renderBookkeepingTool() {
        expenseAmountInput = addInput("输入金额，例如 18.5", 1)
        val categorySpinner = addSpinner(arrayOf("餐饮", "交通", "购物", "学习", "旅行", "其他"))
        expenseNoteInput = addInput("备注，例如 午餐", 1)
        bookkeepingListView = addResultView()
        refreshBookkeeping()
        addButton("记一笔") { addExpense(categorySpinner) }
        addButton("清空账本") {
            viewModel.clearExpenses()
            refreshBookkeeping()
        }
    }

    private fun renderBaseConverterTool() {
        val numberInput = addInput("输入数字", 1)
        addBaseSelector()
        resultView = addResultView()
        addButton("转换") { convertBase(numberInput) }
    }

    private fun requestCameraAndTakePicture(action: String) {
        pendingCameraAction = action
        if (hasCameraPermission()) {
            launchCameraForCurrentAction()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun launchCameraForCurrentAction() {
        try {
            pendingCameraFile = viewModel.createCameraFile(pendingCameraAction)
            pendingCameraUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pendingCameraFile!!)
            cameraPictureLauncher.launch(pendingCameraUri)
        } catch (e: Exception) {
            resultView?.text = "打开相机失败：${e.message}"
        }
    }

    private fun handleCameraImage(uri: Uri) {
        try {
            val action = pendingCameraAction.orEmpty()
            val bitmap = viewModel.loadCapturedCameraBitmap(uri, pendingCameraFile)
            if (action == ToolRepository.TEXT_RECOGNITION) {
                runTextRecognition(bitmap)
                return
            }
            if (viewModel.isImageLabelingTool(action)) {
                runImageLabeling(bitmap, action)
                return
            }

            imagePreview?.setImageBitmap(bitmap)
            viewModel.saveScanBitmap(bitmap)
            val file = pendingCameraFile ?: return
            showFileResult("已存档", file, "image/jpeg", "打开照片")
        } catch (e: Exception) {
            resultView?.text = "拍照处理失败：${e.message}"
        }
    }

    private fun handleImagePicked(uri: Uri) {
        try {
            val action = pendingImageAction.orEmpty()
            if (action == ToolRepository.TEXT_RECOGNITION) {
                runTextRecognition(uri)
                return
            }
            if (viewModel.isImageLabelingTool(action)) {
                runImageLabeling(uri, action)
                return
            }

            val bitmap = viewModel.loadBitmap(uri)
            imagePreview?.setImageBitmap(bitmap)
            when (action) {
                ToolRepository.WATERMARK -> showImageProcessResult(viewModel.addWatermark(bitmap, watermarkInput?.text.toString().trim()))
                ToolRepository.PIXEL_ART -> showImageProcessResult(viewModel.createPixelArt(bitmap))
                ToolRepository.COLORIZE -> showImageProcessResult(viewModel.colorizeImage(bitmap))
            }
        } catch (e: Exception) {
            resultView?.text = "处理失败：${e.message}"
        }
    }

    private fun handleImagesPicked(uris: List<Uri>?) {
        if (uris.isNullOrEmpty()) {
            showToast("没有选择图片")
            return
        }
        try {
            if (pendingMultipleImageAction == ToolRepository.PHOTO_GRID) {
                if (uris.size != 9) {
                    resultView?.text = "请一次选择 9 张图片，按照 3 x 3 合成一张九宫格。当前选择：${uris.size} 张"
                    return
                }
                showImageProcessResult(viewModel.createPhotoGrid(uris))
            } else {
                val file = viewModel.createPdfFromImages(uris)
                showFileResult("PDF 已生成", file, "application/pdf", "预览 PDF")
            }
        } catch (e: Exception) {
            resultView?.text = "处理失败：${e.message}"
        }
    }

    private fun handlePdfPicked(uri: Uri) {
        try {
            if (pendingPdfAction == ToolRepository.PDF_TO_IMAGE) {
                showPdfExportResult(viewModel.exportPdfToImages(uri))
            } else if (pendingPdfAction == ToolRepository.COMPRESS_PDF) {
                val file = viewModel.compressPdf(uri)
                showFileResult("压缩版 PDF 已生成", file, "application/pdf", "预览 PDF")
            }
        } catch (e: Exception) {
            resultView?.text = "PDF 处理失败：${e.message}"
        }
    }

    private fun handleFilePicked(uri: Uri) {
        try {
            val password = passwordInput?.text.toString()
            if (pendingFileAction == ACTION_ENCRYPT_PDF) {
                val file = viewModel.encryptFile(uri, password)
                showFileResult("加密完成", file, "application/octet-stream", "打开加密文件")
            } else if (pendingFileAction == ACTION_DECRYPT_PDF) {
                val file = viewModel.decryptFile(uri, password)
                showFileResult("解密完成", file, "application/pdf", "预览 PDF")
            }
        } catch (e: Exception) {
            resultView?.text = "加密/解密失败：${e.message}"
        }
    }

    private fun runTextRecognition(uri: Uri) {
        resultView?.text = "正在识别文字……"
        viewModel.recognizeTextFromUri(
            uri,
            onPreview = { bitmap -> imagePreview?.setImageBitmap(bitmap) },
            onResult = { result -> resultView?.text = result }
        )
    }

    private fun runTextRecognition(bitmap: Bitmap) {
        resultView?.text = "正在识别文字……"
        viewModel.recognizeTextFromBitmap(
            bitmap,
            onPreview = { previewBitmap -> imagePreview?.setImageBitmap(previewBitmap) },
            onResult = { result -> resultView?.text = result }
        )
    }

    private fun runImageLabeling(uri: Uri, action: String) {
        resultView?.text = "正在识别图片内容……"
        viewModel.recognizeImageFromUri(
            uri,
            action,
            onPreview = { bitmap -> imagePreview?.setImageBitmap(bitmap) },
            onResult = { result -> resultView?.text = result }
        )
    }

    private fun runImageLabeling(bitmap: Bitmap, action: String) {
        resultView?.text = "正在识别图片内容……"
        viewModel.recognizeImageFromBitmap(
            bitmap,
            action,
            onPreview = { previewBitmap -> imagePreview?.setImageBitmap(previewBitmap) },
            onResult = { result -> resultView?.text = result }
        )
    }

    private fun showPdfExportResult(result: PdfExportResult) {
        resultView?.text = result.message
        result.firstImageFile?.let { file ->
            imagePreview?.setImageBitmap(viewModel.loadBitmap(file))
            showAccessButton(file, "image/png", "打开第一张图片")
        }
    }

    private fun showImageProcessResult(result: ImageProcessResult) {
        imagePreview?.setImageBitmap(result.bitmap)
        showFileResult(result.message, result.file, result.mimeType, result.buttonText)
    }

    private fun convertCurrency(amountInput: EditText, fromSpinner: Spinner, toSpinner: Spinner) {
        try {
            resultView?.text = viewModel.convertCurrency(
                amountInput.text.toString(),
                fromSpinner.selectedItem.toString(),
                toSpinner.selectedItem.toString()
            )
        } catch (e: Exception) {
            resultView?.text = "请输入正确金额"
        }
    }

    private fun addTravelItem() {
        val item = travelInput?.text.toString().trim()
        if (item.isEmpty()) {
            showToast("请输入物品名称")
            return
        }
        viewModel.addTravelItem(item)
        travelInput?.setText("")
        refreshTravelList()
    }

    private fun refreshTravelList() {
        travelListView?.text = viewModel.travelItemsText()
    }

    private fun addExpense(categorySpinner: Spinner) {
        try {
            val amount = expenseAmountInput?.text.toString().trim().toDouble()
            val category = categorySpinner.selectedItem.toString()
            val note = expenseNoteInput?.text.toString().trim().ifEmpty { "无备注" }
            viewModel.addExpense(amount, category, note)
            expenseAmountInput?.setText("")
            expenseNoteInput?.setText("")
            refreshBookkeeping()
        } catch (e: Exception) {
            showToast("请输入正确金额")
        }
    }

    private fun refreshBookkeeping() {
        bookkeepingListView?.text = viewModel.bookkeepingSummaryText()
    }

    private fun convertBase(numberInput: EditText) {
        try {
            resultView?.text = viewModel.convertBase(numberInput.text.toString(), selectedBase)
        } catch (e: Exception) {
            resultView?.text = "请输入符合所选进制的数字"
        }
    }

    private fun addBaseSelector() {
        baseOptionViews.clear()
        selectedBase = 10

        val titleView = addText("选择输入数字的进制", 16f, Typeface.BOLD)
        titleView.setTextColor(Color.rgb(25, 25, 25))

        val firstRow = createHorizontalRow()
        val secondRow = createHorizontalRow()
        binding.toolContent.addView(firstRow, matchWrapParams())
        binding.toolContent.addView(secondRow, matchWrapParams())

        firstRow.addView(createBaseOption("二进制", 2), equalWeightParams(0, 6))
        firstRow.addView(createBaseOption("八进制", 8), equalWeightParams(6, 0))
        secondRow.addView(createBaseOption("十进制", 10), equalWeightParams(0, 6))
        secondRow.addView(createBaseOption("十六进制", 16), equalWeightParams(6, 0))

        updateBaseOptionStyle()
    }

    private fun createBaseOption(label: String, base: Int): TextView {
        val optionView = TextView(this)
        optionView.text = "$label\n$base"
        optionView.gravity = Gravity.CENTER
        optionView.textSize = 16f
        optionView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        optionView.setPadding(16, 18, 16, 18)
        optionView.isClickable = true
        optionView.isFocusable = true
        optionView.tag = base
        optionView.setOnClickListener { view ->
            selectedBase = view.tag as Int
            updateBaseOptionStyle()
        }
        baseOptionViews.add(optionView)
        return optionView
    }

    private fun updateBaseOptionStyle() {
        baseOptionViews.forEach { optionView ->
            val base = optionView.tag as Int
            val selected = base == selectedBase
            optionView.setBackgroundResource(if (selected) R.drawable.bg_common_orange_card else R.drawable.bg_home_warm_card)
            optionView.setTextColor(if (selected) Color.WHITE else Color.rgb(25, 25, 25))
        }
    }

    private fun showScanArchiveDialog() {
        val files = viewModel.scanArchiveFiles()
        if (files.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("拍照存档记录")
                .setMessage("现在还没有存档照片，先点击“拍照并存档”保存一张。")
                .setPositiveButton("知道了", null)
                .show()
            return
        }

        val archiveItems = files.mapIndexed { index, file ->
            "${index + 1}. ${viewModel.archiveTimeText(file)}\n${file.name}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("拍照存档记录")
            .setItems(archiveItems) { _, which ->
                openFile(files[which], viewModel.imageMimeType(files[which]))
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun hasPassword(): Boolean {
        if (passwordInput?.text.toString().trim().length < 4) {
            showToast("密码至少 4 位")
            return false
        }
        return true
    }

    private fun showFileResult(message: String, file: File, mimeType: String, buttonText: String) {
        val extraMessage = if (mimeType.startsWith("image/")) "\n已同步写入系统相册。" else ""
        resultView?.text = "$message：\n${file.absolutePath}$extraMessage"
        showAccessButton(file, mimeType, buttonText)
    }

    private fun showAccessButton(file: File, mimeType: String, buttonText: String) {
        accessButton?.let { button -> binding.toolContent.removeView(button) }
        accessButton = addButton(buttonText) { openFile(file, mimeType) }
    }

    private fun openFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(Intent.createChooser(intent, "选择打开方式"))
        } catch (e: Exception) {
            showToast("没有找到可以打开该文件的应用")
        }
    }

    private fun startCompass() {
        if (!compassActive) {
            return
        }

        val manager = sensorManager
        val accelerometerSensor = accelerometer
        val magneticSensor = magneticField
        if (manager == null || accelerometerSensor == null || magneticSensor == null) {
            compassView?.text = "无法读取方向"
            return
        }
        manager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        manager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onResume() {
        super.onResume()
        if (compassActive) {
            startCompass()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, gravityValues.size)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, magneticValues.size)
            hasMagnetic = true
        }

        if (hasGravity && hasMagnetic && compassView != null) {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
                degree = (degree + 360) % 360
                compassView?.text = String.format(Locale.CHINA, "%.0f°  %s", degree, directionName(degree))
                compassDialView?.setDegree(degree)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun directionName(degree: Float): String {
        return when {
            degree >= 337.5f || degree < 22.5f -> "北"
            degree < 67.5f -> "东北"
            degree < 112.5f -> "东"
            degree < 157.5f -> "东南"
            degree < 202.5f -> "南"
            degree < 247.5f -> "西南"
            degree < 292.5f -> "西"
            else -> "西北"
        }
    }

    private fun addText(text: String, sizeSp: Float, style: Int): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(Color.rgb(25, 25, 25))
        textView.textSize = sizeSp
        textView.setTypeface(Typeface.DEFAULT, style)
        textView.setLineSpacing(6f, 1.0f)
        binding.toolContent.addView(textView, matchWrapParams())
        return textView
    }

    private fun addDescription(text: String) {
        val textView = addText(text, 15f, Typeface.NORMAL)
        textView.setTextColor(Color.rgb(109, 109, 109))
    }

    private fun addResultView(): TextView {
        val textView = addText("结果会显示在这里。", 15f, Typeface.NORMAL)
        textView.setPadding(18, 18, 18, 18)
        textView.setBackgroundResource(R.drawable.bg_note_input)
        return textView
    }

    private fun addButton(text: String, listener: View.OnClickListener): Button {
        val button = Button(this)
        button.text = text
        button.isAllCaps = false
        button.setTextColor(Color.rgb(25, 25, 25))
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        button.setBackgroundResource(R.drawable.bg_home_warm_card)
        button.setOnClickListener(listener)
        binding.toolContent.addView(button, matchWrapParams())
        return button
    }

    private fun addInput(hint: String, minLines: Int): EditText {
        val editText = EditText(this)
        editText.hint = hint
        editText.minLines = minLines
        editText.setTextColor(Color.rgb(25, 25, 25))
        editText.setHintTextColor(Color.rgb(153, 153, 153))
        editText.setBackgroundResource(R.drawable.bg_note_input)
        editText.setPadding(18, 12, 18, 12)
        binding.toolContent.addView(editText, matchWrapParams())
        return editText
    }

    private fun addSpinner(items: Array<String>): Spinner {
        val spinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        binding.toolContent.addView(spinner, matchWrapParams())
        return spinner
    }

    private fun addImagePreview(): ImageView {
        val imageView = ImageView(this)
        imageView.adjustViewBounds = true
        imageView.minimumHeight = 240
        imageView.setBackgroundResource(R.drawable.bg_note_input)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.toolContent.addView(imageView, matchWrapParams())
        return imageView
    }

    private fun createHorizontalRow(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.isBaselineAligned = false
        return row
    }

    private fun matchWrapParams(): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 14, 0, 0)
        return params
    }

    private fun equalWeightParams(marginStart: Int, marginEnd: Int): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        )
        params.setMargins(marginStart, 0, marginEnd, 0)
        return params
    }

    private fun compassLayoutParams(): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(300f)
        )
        params.setMargins(0, 20, 0, 0)
        return params
    }

    private fun dp(value: Float): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TOOL_ID = "tool_id"
        private const val ACTION_ENCRYPT_PDF = "encrypt_pdf"
        private const val ACTION_DECRYPT_PDF = "decrypt_pdf"

        fun open(context: Context, toolId: String) {
            val intent = Intent(context, ToolActivity::class.java)
            intent.putExtra(EXTRA_TOOL_ID, toolId)
            context.startActivity(intent)
        }
    }
}
