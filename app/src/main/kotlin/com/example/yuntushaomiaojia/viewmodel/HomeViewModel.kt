package com.example.yuntushaomiaojia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yuntushaomiaojia.data.tool.ToolRepository
import com.example.yuntushaomiaojia.model.Event

class HomeViewModel : ViewModel() {

    private val _openToolEvent = MutableLiveData<Event<String>>()
    val openToolEvent: LiveData<Event<String>> = _openToolEvent

    fun openScanArchive() = openTool(ToolRepository.SCAN_ARCHIVE)

    fun openTextRecognition() = openTool(ToolRepository.TEXT_RECOGNITION)

    fun openPlantRecognition() = openTool(ToolRepository.PLANT_RECOGNITION)

    fun openFruitRecognition() = openTool(ToolRepository.FRUIT_RECOGNITION)

    fun openAnimalRecognition() = openTool(ToolRepository.ANIMAL_RECOGNITION)

    fun openPdfToImage() = openTool(ToolRepository.PDF_TO_IMAGE)

    fun openImageToPdf() = openTool(ToolRepository.IMAGE_TO_PDF)

    fun openEncryptPdf() = openTool(ToolRepository.ENCRYPT_PDF)

    fun openCompressPdf() = openTool(ToolRepository.COMPRESS_PDF)

    private fun openTool(toolId: String) {
        _openToolEvent.value = Event(toolId)
    }
}
