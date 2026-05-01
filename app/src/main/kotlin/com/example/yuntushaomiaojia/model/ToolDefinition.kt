package com.example.yuntushaomiaojia.model

import androidx.annotation.StringRes

data class ToolDefinition(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val category: ToolCategory
)
