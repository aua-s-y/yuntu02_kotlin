package com.example.yuntushaomiaojia.data.recognition

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class TextRecognitionRepository {

    fun recognizeText(
        image: InputImage,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            .process(image)
            .addOnSuccessListener { text ->
                onSuccess(text.text.ifBlank { "未识别到文字" })
            }
            .addOnFailureListener { error ->
                onFailure("文字识别失败：${error.message}")
            }
    }
}
