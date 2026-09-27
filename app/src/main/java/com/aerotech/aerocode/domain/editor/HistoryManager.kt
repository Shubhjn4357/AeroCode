package com.aerotech.aerocode.domain.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class EditTransaction(
    val previousText: String,
    val previousSelection: TextRange,
    val newText: String,
    val newSelection: TextRange,
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryManager(private val maxHistory: Int = 100) {
    private val undoStack = mutableListOf<EditTransaction>()
    private val redoStack = mutableListOf<EditTransaction>()
    private var lastTransactionTime: Long = 0L

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun recordEdit(oldValue: TextFieldValue, newValue: TextFieldValue) {
        if (oldValue.text == newValue.text) return // Selection only change, don't push undo

        val now = System.currentTimeMillis()
        // If typing rapidly within 500ms and length difference is 1, coalesce the transaction
        val shouldCoalesce = (now - lastTransactionTime < 500L) &&
                undoStack.isNotEmpty() &&
                Math.abs(oldValue.text.length - newValue.text.length) == 1

        if (shouldCoalesce) {
            val last = undoStack.removeAt(undoStack.size - 1)
            undoStack.add(
                EditTransaction(
                    previousText = last.previousText,
                    previousSelection = last.previousSelection,
                    newText = newValue.text,
                    newSelection = newValue.selection,
                    timestamp = now
                )
            )
        } else {
            undoStack.add(
                EditTransaction(
                    previousText = oldValue.text,
                    previousSelection = oldValue.selection,
                    newText = newValue.text,
                    newSelection = newValue.selection,
                    timestamp = now
                )
            )
            if (undoStack.size > maxHistory) undoStack.removeAt(0)
        }

        redoStack.clear()
        lastTransactionTime = now
    }

    fun undo(current: TextFieldValue): TextFieldValue? {
        if (undoStack.isEmpty()) return null
        val tx = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(
            EditTransaction(
                previousText = current.text,
                previousSelection = current.selection,
                newText = tx.previousText,
                newSelection = tx.previousSelection,
                timestamp = System.currentTimeMillis()
            )
        )
        return TextFieldValue(
            text = tx.previousText,
            selection = tx.previousSelection
        )
    }

    fun redo(current: TextFieldValue): TextFieldValue? {
        if (redoStack.isEmpty()) return null
        val tx = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(
            EditTransaction(
                previousText = current.text,
                previousSelection = current.selection,
                newText = tx.previousText,
                newSelection = tx.previousSelection,
                timestamp = System.currentTimeMillis()
            )
        )
        return TextFieldValue(
            text = tx.previousText,
            selection = tx.previousSelection
        )
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        lastTransactionTime = 0L
    }
}
