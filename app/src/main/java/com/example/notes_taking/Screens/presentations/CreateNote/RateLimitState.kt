package com.example.notes_taking.Screens.presentations.Editor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RateLimitState(
    val usedCount: Int = 0,
    val isLimited: Boolean = false,
    val secondsRemaining: Int = 0,
) {
    val remainingCalls: Int get() = MAX_CALLS - usedCount

    companion object {
        const val MAX_CALLS = 3
        const val WINDOW_MS = 2 * 60 * 1000L // دقيقتين
    }
}

class AiRateLimiter {

    // ======= Singleton =======
    companion object {
        @Volatile
        private var INSTANCE: AiRateLimiter? = null

        fun getInstance(): AiRateLimiter =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiRateLimiter().also { INSTANCE = it }
            }
    }

    private val _state = MutableStateFlow(RateLimitState())
    val state = _state.asStateFlow()

    private val callTimestamps = ArrayDeque<Long>()

    fun tryConsume(): Boolean {
        val now = System.currentTimeMillis()
        pruneOldCalls(now)

        return if (callTimestamps.size < RateLimitState.MAX_CALLS) {
            callTimestamps.addLast(now)
            _state.update {
                it.copy(
                    usedCount        = callTimestamps.size,
                    isLimited        = false,
                    secondsRemaining = 0,
                )
            }
            true
        } else {
            val oldestCall   = callTimestamps.first()
            val windowEndsAt = oldestCall + RateLimitState.WINDOW_MS
            val secondsLeft  = ((windowEndsAt - now) / 1000).toInt().coerceAtLeast(1)
            _state.update {
                it.copy(
                    usedCount        = callTimestamps.size,
                    isLimited        = true,
                    secondsRemaining = secondsLeft,
                )
            }
            false
        }
    }

    fun tick() {
        val now = System.currentTimeMillis()
        pruneOldCalls(now)

        val limited     = callTimestamps.size >= RateLimitState.MAX_CALLS
        val secondsLeft = if (limited) {
            val windowEndsAt = callTimestamps.first() + RateLimitState.WINDOW_MS
            ((windowEndsAt - now) / 1000).toInt().coerceAtLeast(0)
        } else 0

        _state.update {
            it.copy(
                usedCount        = callTimestamps.size,
                isLimited        = limited && secondsLeft > 0,
                secondsRemaining = secondsLeft,
            )
        }
    }

    private fun pruneOldCalls(now: Long) {
        val cutoff = now - RateLimitState.WINDOW_MS
        while (callTimestamps.isNotEmpty() && callTimestamps.first() < cutoff) {
            callTimestamps.removeFirst()
        }
    }
}