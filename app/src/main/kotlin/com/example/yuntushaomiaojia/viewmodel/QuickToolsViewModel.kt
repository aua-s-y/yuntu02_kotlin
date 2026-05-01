package com.example.yuntushaomiaojia.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yuntushaomiaojia.R
import com.example.yuntushaomiaojia.data.tool.ToolRepository
import com.example.yuntushaomiaojia.model.Event
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class QuickToolsViewModel : ViewModel() {

    private var activeTranslator: Translator? = null

    private val _translateState = MutableLiveData<TranslateState>()
    val translateState: LiveData<TranslateState> = _translateState

    private val _toastEvent = MutableLiveData<Event<Int>>()
    val toastEvent: LiveData<Event<Int>> = _toastEvent

    private val _swapLanguageEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val swapLanguageEvent: LiveData<Event<Pair<Int, Int>>> = _swapLanguageEvent

    private val _openToolEvent = MutableLiveData<Event<String>>()
    val openToolEvent: LiveData<Event<String>> = _openToolEvent

    fun swapLanguage(sourcePosition: Int, targetPosition: Int) {
        _swapLanguageEvent.value = Event(targetPosition to sourcePosition)
    }

    fun translate(input: String, sourcePosition: Int, targetPosition: Int) {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            showToast(R.string.translate_input_empty)
            return
        }
        if (sourcePosition == targetPosition) {
            _translateState.value = TranslateState(text = trimmedInput)
            return
        }
        val sourceCode = toMlKitLanguage(sourcePosition)
        val targetCode = toMlKitLanguage(targetPosition)
        if (sourceCode == null || targetCode == null) {
            _translateState.value = TranslateState(messageRes = R.string.translate_not_supported)
            return
        }
        translateWithModel(sourceCode, targetCode, trimmedInput)
    }

    fun openPixelArt() = openTool(ToolRepository.PIXEL_ART)

    fun openColorize() = openTool(ToolRepository.COLORIZE)

    fun openTravelList() = openTool(ToolRepository.TRAVEL_LIST)

    fun openFontZoom() = openTool(ToolRepository.FONT_ZOOM)

    fun openCompass() = openTool(ToolRepository.COMPASS)

    fun openBookkeeping() = openTool(ToolRepository.BOOKKEEPING)

    fun openExchangeRate() = openTool(ToolRepository.EXCHANGE_RATE)

    fun openBaseConverter() = openTool(ToolRepository.BASE_CONVERTER)

    private fun translateWithModel(sourceCode: String, targetCode: String, input: String) {
        closeActiveTranslator()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()
        val translator = Translation.getClient(options)
        activeTranslator = translator

        _translateState.value = TranslateState(messageRes = R.string.translate_model_preparing)
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
            .addOnSuccessListener {
                if (!isActiveTranslator(translator)) {
                    return@addOnSuccessListener
                }
                translator.translate(input)
                    .addOnSuccessListener { result ->
                        if (isActiveTranslator(translator)) {
                            _translateState.value = TranslateState(text = result)
                        }
                    }
                    .addOnFailureListener { exception -> showTranslateFailure(translator, exception) }
            }
            .addOnFailureListener { exception -> showTranslateFailure(translator, exception) }
    }

    private fun showTranslateFailure(translator: Translator, exception: Exception) {
        if (isActiveTranslator(translator)) {
            _translateState.value = TranslateState(errorDetail = exception.message.orEmpty())
        }
    }

    private fun isActiveTranslator(translator: Translator): Boolean {
        return activeTranslator == translator
    }

    private fun toMlKitLanguage(position: Int): String? {
        return when (position) {
            LANGUAGE_CHINESE -> TranslateLanguage.CHINESE
            LANGUAGE_JAPANESE -> TranslateLanguage.JAPANESE
            LANGUAGE_KOREAN -> TranslateLanguage.KOREAN
            LANGUAGE_RUSSIAN -> TranslateLanguage.RUSSIAN
            else -> null
        }
    }

    private fun openTool(toolId: String) {
        _openToolEvent.value = Event(toolId)
    }

    private fun showToast(@StringRes messageRes: Int) {
        _toastEvent.value = Event(messageRes)
    }

    private fun closeActiveTranslator() {
        activeTranslator?.close()
        activeTranslator = null
    }

    override fun onCleared() {
        super.onCleared()
        closeActiveTranslator()
    }

    data class TranslateState(
        @param:StringRes val messageRes: Int? = null,
        val text: String? = null,
        val errorDetail: String? = null
    )

    companion object {
        private const val LANGUAGE_CHINESE = 0
        private const val LANGUAGE_JAPANESE = 1
        private const val LANGUAGE_KOREAN = 2
        private const val LANGUAGE_RUSSIAN = 3
    }
}
