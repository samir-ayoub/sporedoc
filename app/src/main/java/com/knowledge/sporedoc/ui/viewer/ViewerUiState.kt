package com.knowledge.sporedoc.ui.viewer

import com.knowledge.sporedoc.domain.model.CropRect

/**
 * The ViewerScreen's whole visible state: file name, page position, dialog visibility,
 * rotate mode, crop mode.
 */
data class ViewerUiState(
    val fileName: String = "",
    val pageCount: Int = 0,
    val currentPage: Int = 1,
    val isDeleteDialogVisible: Boolean = false,
    val isRotateModeActive: Boolean = false,
    val pageRotationDegrees: Map<Int, Int> = emptyMap(),
    val contentRevision: Int = 0,
    val isCropModeActive: Boolean = false,
    val cropRect: CropRect? = null
)
