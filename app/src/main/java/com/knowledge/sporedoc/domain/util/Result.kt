package com.knowledge.sporedoc.domain.util

/** Generic typed result used by every operation that can fail with a known reason. */
sealed class Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>()
    data class Failure<E>(val error: E) : Result<Nothing, E>()
}
