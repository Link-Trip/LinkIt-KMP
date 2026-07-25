package com.linkit.company.domain

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/** 즉시 완료되는 fake suspend 함수로 구성된 도메인 단위 테스트를 실행한다. */
internal fun <T> runImmediateSuspend(block: suspend () -> T): T {
    var outcome: Result<T>? = null
    block.startCoroutine(
        object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                outcome = result
            }
        },
    )

    return checkNotNull(outcome) { "The test coroutine did not complete immediately" }.getOrThrow()
}
