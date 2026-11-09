package com.mypen.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Replaces scattered `Toast` calls with Material 3 snackbars that support an
 * action (for example "Undo" after deleting a pen) and stay accessible to
 * screen readers.
 */
class AppMessages internal constructor(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    fun info(message: String) {
        scope.launch { hostState.showSnackbar(message = message, withDismissAction = true) }
    }

    fun success(message: String) {
        scope.launch {
            hostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun error(message: String) {
        scope.launch {
            hostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun undo(message: String, actionLabel: String, onUndo: () -> Unit) {
        scope.launch {
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndo()
            }
        }
    }
}

val LocalAppMessages = staticCompositionLocalOf<AppMessages> {
    error("LocalAppMessages is not provided. Wrap the UI in MyPenAppScaffold.")
}

@Composable
fun rememberAppMessages(): AppMessages {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) { AppMessages(hostState, scope) }
}

/** Snackbar host styled to match the neutral dark palette. */
@Composable
fun AppSnackbarHost(data: SnackbarData, modifier: Modifier = Modifier) {
    Snackbar(
        snackbarData = data,
        modifier = modifier.padding(12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actionContentColor = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small
    )
}
