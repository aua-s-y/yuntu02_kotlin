package com.example.yuntushaomiaojia.data.image

import android.graphics.Bitmap
import java.io.File

data class ImageProcessResult(
    val bitmap: Bitmap,
    val file: File,
    val message: String,
    val mimeType: String,
    val buttonText: String
)
