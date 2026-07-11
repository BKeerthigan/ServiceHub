package com.example.servicehub.utils

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.composed

private const val THROTTLE_MS = 600L

/**
 * Returns a click handler that ignores taps within THROTTLE_MS of the previous one.
 * Use for Button(onClick = rememberThrottledClick { ... })
 */
@Composable
fun rememberThrottledClick(onClick: () -> Unit): () -> Unit {
    var lastMs by remember { mutableLongStateOf(0L) }
    return {
        val now = System.currentTimeMillis()
        if (now - lastMs > THROTTLE_MS) {
            lastMs = now
            onClick()
        }
    }
}

/**
 * Modifier version for Modifier.clickable blocks.
 * Use .throttledClickable { ... } instead of .clickable { ... }
 */
fun Modifier.throttledClickable(onClick: () -> Unit): Modifier = composed {
    var lastMs by remember { mutableLongStateOf(0L) }
    clickable {
        val now = System.currentTimeMillis()
        if (now - lastMs > THROTTLE_MS) {
            lastMs = now
            onClick()
        }
    }
}
