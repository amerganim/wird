package com.wird.core.common.dispatchers

import javax.inject.Qualifier

/** Qualifier for injecting specific [kotlinx.coroutines.CoroutineDispatcher]s. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: WirdDispatcher)

enum class WirdDispatcher {
    Default,
    IO,
}
