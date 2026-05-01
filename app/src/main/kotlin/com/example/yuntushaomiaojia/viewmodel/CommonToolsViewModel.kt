package com.example.yuntushaomiaojia.viewmodel

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yuntushaomiaojia.R
import com.example.yuntushaomiaojia.data.notebook.NotebookRepository
import com.example.yuntushaomiaojia.data.qrcode.QrCodeRepository
import com.example.yuntushaomiaojia.data.tool.ToolRepository
import com.example.yuntushaomiaojia.model.Event

class CommonToolsViewModel(
    private val notebookRepository: NotebookRepository,
    private val qrCodeRepository: QrCodeRepository = QrCodeRepository()
) : ViewModel() {

    private val _noteText = MutableLiveData<String>()
    val noteText: LiveData<String> = _noteText

    private val _toastEvent = MutableLiveData<Event<Int>>()
    val toastEvent: LiveData<Event<Int>> = _toastEvent

    private val _savedNoteHistoryEvent = MutableLiveData<Event<String>>()
    val savedNoteHistoryEvent: LiveData<Event<String>> = _savedNoteHistoryEvent

    private val _qrBitmapEvent = MutableLiveData<Event<Bitmap>>()
    val qrBitmapEvent: LiveData<Event<Bitmap>> = _qrBitmapEvent

    private val _qrInputErrorEvent = MutableLiveData<Event<Int>>()
    val qrInputErrorEvent: LiveData<Event<Int>> = _qrInputErrorEvent

    private val _openToolEvent = MutableLiveData<Event<String>>()
    val openToolEvent: LiveData<Event<String>> = _openToolEvent

    fun loadSavedNotebookContent() {
        _noteText.value = notebookRepository.getCurrentNote()
    }

    fun saveNote(content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) {
            showToast(R.string.note_empty)
            return
        }
        notebookRepository.saveNote(trimmedContent)
        showToast(R.string.note_saved)
    }

    fun showSavedNote() {
        val savedContent = notebookRepository.getNoteHistory()
        if (savedContent.trim().isEmpty()) {
            showToast(R.string.note_empty)
            return
        }
        _savedNoteHistoryEvent.value = Event(savedContent)
    }

    fun generateQrCode(content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) {
            _qrInputErrorEvent.value = Event(R.string.common_qr_dialog_hint)
            return
        }
        val bitmap = qrCodeRepository.createQrBitmap(trimmedContent, QR_SIZE)
        if (bitmap == null) {
            showToast(R.string.common_qr_generate_failed)
            return
        }
        _qrBitmapEvent.value = Event(bitmap)
    }

    fun openCompass() = openTool(ToolRepository.COMPASS)

    fun openExchangeRate() = openTool(ToolRepository.EXCHANGE_RATE)

    fun openWatermark() = openTool(ToolRepository.WATERMARK)

    fun openPhotoGrid() = openTool(ToolRepository.PHOTO_GRID)

    private fun openTool(toolId: String) {
        _openToolEvent.value = Event(toolId)
    }

    private fun showToast(@StringRes messageRes: Int) {
        _toastEvent.value = Event(messageRes)
    }

    class Factory(
        private val notebookRepository: NotebookRepository,
        private val qrCodeRepository: QrCodeRepository = QrCodeRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CommonToolsViewModel(notebookRepository, qrCodeRepository) as T
        }
    }

    companion object {
        private const val QR_SIZE = 520
    }
}
