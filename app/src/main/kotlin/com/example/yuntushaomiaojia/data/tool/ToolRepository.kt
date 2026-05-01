package com.example.yuntushaomiaojia.data.tool

import android.content.Context
import com.example.yuntushaomiaojia.R
import com.example.yuntushaomiaojia.model.ToolCategory
import com.example.yuntushaomiaojia.model.ToolDefinition

class ToolRepository {

    private val tools = listOf(
        ToolDefinition(SCAN_ARCHIVE, R.string.home_scan_archive, R.string.tool_desc_scan_archive, ToolCategory.SCAN_RECOGNITION),
        ToolDefinition(TEXT_RECOGNITION, R.string.home_text_recognition, R.string.tool_desc_text_recognition, ToolCategory.SCAN_RECOGNITION),
        ToolDefinition(PLANT_RECOGNITION, R.string.home_plant_recognition, R.string.tool_desc_image_recognition, ToolCategory.SCAN_RECOGNITION),
        ToolDefinition(FRUIT_RECOGNITION, R.string.home_fruit_recognition, R.string.tool_desc_image_recognition, ToolCategory.SCAN_RECOGNITION),
        ToolDefinition(ANIMAL_RECOGNITION, R.string.home_animal_recognition, R.string.tool_desc_image_recognition, ToolCategory.SCAN_RECOGNITION),
        ToolDefinition(PDF_TO_IMAGE, R.string.home_pdf_to_image, R.string.tool_desc_pdf_to_image, ToolCategory.PDF),
        ToolDefinition(IMAGE_TO_PDF, R.string.home_image_to_pdf, R.string.tool_desc_image_to_pdf, ToolCategory.PDF),
        ToolDefinition(ENCRYPT_PDF, R.string.home_encrypt_pdf, R.string.tool_desc_encrypt_pdf, ToolCategory.PDF),
        ToolDefinition(COMPRESS_PDF, R.string.home_compress_pdf, R.string.tool_desc_compress_pdf, ToolCategory.PDF),
        ToolDefinition(COMPASS, R.string.common_compass, R.string.tool_desc_compass, ToolCategory.EFFICIENCY),
        ToolDefinition(EXCHANGE_RATE, R.string.common_exchange_rate, R.string.tool_desc_exchange_rate, ToolCategory.CONVERTER),
        ToolDefinition(WATERMARK, R.string.common_watermark, R.string.tool_desc_watermark, ToolCategory.IMAGE_CREATION),
        ToolDefinition(PHOTO_GRID, R.string.common_photo_grid, R.string.tool_desc_photo_grid, ToolCategory.IMAGE_CREATION),
        ToolDefinition(PIXEL_ART, R.string.quick_pixel_art, R.string.tool_desc_pixel_art, ToolCategory.IMAGE_CREATION),
        ToolDefinition(COLORIZE, R.string.quick_colorize, R.string.tool_desc_colorize, ToolCategory.IMAGE_CREATION),
        ToolDefinition(TRAVEL_LIST, R.string.quick_travel_list, R.string.tool_desc_travel_list, ToolCategory.LIFE),
        ToolDefinition(FONT_ZOOM, R.string.quick_font_zoom, R.string.tool_desc_font_zoom, ToolCategory.EFFICIENCY),
        ToolDefinition(BOOKKEEPING, R.string.quick_bookkeeping, R.string.tool_desc_bookkeeping, ToolCategory.LIFE),
        ToolDefinition(BASE_CONVERTER, R.string.quick_base_converter, R.string.tool_desc_base_converter, ToolCategory.CONVERTER)
    ).associateBy { tool -> tool.id }

    fun getTool(toolId: String): ToolDefinition? = tools[toolId]

    fun getTitleText(context: Context, toolId: String): String {
        val titleRes = getTool(toolId)?.titleRes ?: R.string.tool_detail_default_title
        return context.getString(titleRes)
    }

    fun getDescriptionText(context: Context, toolId: String): String {
        val descriptionRes = getTool(toolId)?.descriptionRes ?: R.string.tool_desc_default
        return context.getString(descriptionRes)
    }

    fun cameraFolderName(toolId: String): String {
        return if (toolId == SCAN_ARCHIVE) SCAN_ARCHIVE else CAMERA_RECOGNITION_FOLDER
    }

    fun supportsCameraRecognition(toolId: String): Boolean {
        return toolId == TEXT_RECOGNITION || isImageLabelingTool(toolId)
    }

    fun isImageLabelingTool(toolId: String?): Boolean {
        return toolId == PLANT_RECOGNITION || toolId == FRUIT_RECOGNITION || toolId == ANIMAL_RECOGNITION
    }

    companion object {
        const val SCAN_ARCHIVE = "scan_archive"
        const val TEXT_RECOGNITION = "text_recognition"
        const val PLANT_RECOGNITION = "plant_recognition"
        const val FRUIT_RECOGNITION = "fruit_recognition"
        const val ANIMAL_RECOGNITION = "animal_recognition"
        const val PDF_TO_IMAGE = "pdf_to_image"
        const val IMAGE_TO_PDF = "image_to_pdf"
        const val ENCRYPT_PDF = "encrypt_pdf"
        const val COMPRESS_PDF = "compress_pdf"
        const val COMPASS = "compass"
        const val EXCHANGE_RATE = "exchange_rate"
        const val WATERMARK = "watermark"
        const val PHOTO_GRID = "photo_grid"
        const val PIXEL_ART = "pixel_art"
        const val COLORIZE = "colorize"
        const val TRAVEL_LIST = "travel_list"
        const val FONT_ZOOM = "font_zoom"
        const val BOOKKEEPING = "bookkeeping"
        const val BASE_CONVERTER = "base_converter"
        private const val CAMERA_RECOGNITION_FOLDER = "camera_recognition"
    }
}
