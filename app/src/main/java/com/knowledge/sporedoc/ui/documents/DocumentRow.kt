package com.knowledge.sporedoc.ui.documents

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * Renders one [DocumentRowUi]: framed thumbnail, file name, metadata line, and the
 * trailing divider (`components.DocumentRow.dividerBetweenRows`).
 */
@Composable
fun DocumentRow(
    row: DocumentRowUi,
    loadThumbnail: suspend (DocumentId) -> PageImage?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = SporeDocSpacing.rowPaddingStartDp,
                    end = SporeDocSpacing.rowPaddingEndDp,
                    top = SporeDocSpacing.rowPaddingVerticalDp,
                    bottom = SporeDocSpacing.rowPaddingVerticalDp
                ),
            verticalAlignment = Alignment.Top
        ) {
            DocumentThumbnail(id = row.id, loadThumbnail = loadThumbnail)
            Column(modifier = Modifier.padding(start = SporeDocSpacing.rowGapThumbToTextDp)) {
                Text(
                    text = row.fileName,
                    style = SporeDocTypography.fileName,
                    maxLines = SporeDocTypography.fileNameMaxLines,
                    overflow = SporeDocTypography.fileNameOverflow
                )
                Text(
                    text = row.metaLine,
                    style = SporeDocTypography.fileMeta,
                    modifier = Modifier.padding(top = SporeDocSpacing.textColumnGapDp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SporeDocSpacing.rowDividerInsetDp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SporeDocStroke.hairlineDp)
                    .background(SporeDocColors.goldDividerSoft)
            )
        }
    }
}

/** Lazily loads and renders the document's first-page thumbnail; nothing is shown on failure (R7). */
@Composable
private fun DocumentThumbnail(
    id: DocumentId,
    loadThumbnail: suspend (DocumentId) -> PageImage?
) {
    val pageImage by produceState<PageImage?>(initialValue = null, key1 = id) {
        value = loadThumbnail(id)
    }
    Box(
        modifier = Modifier
            .border(SporeDocStroke.frameDp, SporeDocColors.goldMatte)
            .padding(SporeDocSpacing.thumbnailFramePaddingDp)
    ) {
        Box(
            modifier = Modifier
                .size(width = SporeDocSpacing.thumbnailWidthDp, height = SporeDocSpacing.thumbnailHeightDp)
                .background(SporeDocColors.paperThumb)
        ) {
            val currentImage = pageImage
            if (currentImage != null) {
                val bitmap = remember(currentImage) {
                    BitmapFactory.decodeByteArray(currentImage.bytes, 0, currentImage.bytes.size)
                        ?.asImageBitmap()
                }
                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier.size(
                            width = SporeDocSpacing.thumbnailWidthDp,
                            height = SporeDocSpacing.thumbnailHeightDp
                        ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
