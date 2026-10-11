package com.knowledge.sporedoc.ui.viewer

/** The ViewerScreen's whole visible state: file name, page position, dialog visibility. */
data class ViewerUiState(
    val fileName: String = "",
    val pageCount: Int = 0,
    val currentPage: Int = 1,
    val isDeleteDialogVisible: Boolean = false
)
