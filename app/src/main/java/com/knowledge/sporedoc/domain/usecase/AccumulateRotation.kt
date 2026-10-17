package com.knowledge.sporedoc.domain.usecase

/**
 * SD-22 R3/R4: one ROTATE LEFT tap adds 90 degrees counterclockwise to the running total,
 * wrapping at 360 degrees, so four consecutive taps return a page to the orientation it had
 * when the rotate-mode state was entered.
 */
class AccumulateRotation {
    operator fun invoke(currentDegrees: Int): Int = (currentDegrees + 90) % 360
}
