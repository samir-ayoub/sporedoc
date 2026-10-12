package com.knowledge.sporedoc.ui.documents

/**
 * Decides, from the Documents list's current scroll position, whether the header should be
 * in its collapsed (compact bar) state or its expanded (full) state (SD-16, R1/R2/R5).
 *
 * The list is considered "at rest at the top" only when [firstVisibleItemIndex] and
 * [firstVisibleItemScrollOffset] are both zero; any other combination means the list has
 * scrolled away from the top, so the header must collapse. The parameters mirror
 * `LazyListState.firstVisibleItemIndex` / `.firstVisibleItemScrollOffset` so callers can pass
 * those values straight through, keeping this function free of Compose types.
 */
fun isDocumentsHeaderCollapsed(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int
): Boolean {
    return !(firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0)
}
