package com.wird.core.common.result

/**
 * A minimal result wrapper for operations that can fail. Repositories can map
 * their [kotlinx.coroutines.flow.Flow]s into this type for the UI layer.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val throwable: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}
