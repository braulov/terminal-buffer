package org.example

import org.example.model.*
import org.example.storage.LineHistory
import org.example.storage.RingLineHistory

class TerminalBuffer(
    private var width: Int,
    private var height: Int,
    private var scrollbackMaxSize: Int
) {

    init {
        require(width > 0)
        require(height > 0)
        require(scrollbackMaxSize >= 0)
    }

    private var attributes = Attributes()
    private var cursor = Cursor()
    private var history: LineHistory = RingLineHistory(width, height, scrollbackMaxSize)

    private var virtualBlankScreenRows = 0

    // ================= CURSOR =================

    fun getCursorPosition(): Position = cursor.getPosition()

    fun setCursorPosition(position: Position) {
        val col = position.column.coerceIn(0, width - 1)
        val row = position.row.coerceIn(0, height - 1)
        cursor.setPosition(Position(col, row))
    }

    fun moveCursor(type: MoveType, count: Int = 1) {
        require(count >= 0)

        val p = cursor.getPosition()
        val newPos = when (type) {
            MoveType.UP -> Position(p.column, (p.row - count).coerceAtLeast(0))
            MoveType.DOWN -> Position(p.column, (p.row + count).coerceAtMost(height - 1))
            MoveType.LEFT -> Position((p.column - count).coerceAtLeast(0), p.row)
            MoveType.RIGHT -> Position((p.column + count).coerceAtMost(width - 1), p.row)
        }
        cursor.setPosition(newPos)
    }

    // ================= WRITE =================

    fun writeText(text: String) {
        if (text.isEmpty()) return

        val pos = cursor.getPosition()
        val line = ensureWritableScreenLine(pos.row)

        var column = pos.column
        var written = 0

        for (ch in text) {
            if (column >= width) break
            line.cells[column] = Cell(ch, attributes)
            column++
            written++
        }

        val newColumn = (pos.column + written).coerceAtMost(width - 1)
        cursor.setPosition(Position(newColumn, pos.row))
    }

    // ================= SCREEN OPS =================

    fun insertEmptyLineAtBottom() {
        consumeVirtualRowIfPresent()
        history.appendLine(blankLine())
    }

    fun clearScreen() {
        val preservedScrollback = scrollbackSize()

        val newHistory = RingLineHistory(width, height, scrollbackMaxSize)
        repeat(preservedScrollback) {
            newHistory.appendLine(history.getLine(it))
        }

        history = newHistory
        virtualBlankScreenRows = height
        cursor = Cursor()
    }

    fun clearScreenAndScrollback() {
        history.clear()
        virtualBlankScreenRows = 0
        cursor = Cursor()
    }

    // ================= READ =================

    fun getScreen(): String =
        screenLines().joinToString("\n") { it.asPlainString() }

    fun getScreenAndScrollback(): String {
        val lines = mutableListOf<Line>()
        repeat(scrollbackSize()) {
            lines.add(history.getLine(it))
        }
        lines.addAll(screenLines())
        return lines.joinToString("\n") { it.asPlainString() }
    }

    fun getLine(row: Int): String =
        screenLines()[row].asPlainString()

    // ================= INTERNAL =================

    private fun ensureWritableScreenLine(screenRow: Int): Line {
        require(screenRow in 0 until height)

        val blankLines = blankScreenLineCount()

        return if (screenRow < blankLines) {
            val toMaterialize = blankLines - screenRow

            repeat(toMaterialize) {
                history.appendLine(blankLine())
                consumeVirtualRowIfPresent()
            }

            history.getLine(history.size - realScreenLineCount())
        } else {
            val idx = screenStartIndex() + (screenRow - blankLines)
            history.getLine(idx)
        }
    }

    private fun consumeVirtualRowIfPresent() {
        if (virtualBlankScreenRows > 0) virtualBlankScreenRows--
    }

    private fun totalVisibleRows(): Int = scrollbackSize() + height

    private fun realScreenCapacity(): Int = height - virtualBlankScreenRows

    private fun realScreenLineCount(): Int =
        minOf(history.size, realScreenCapacity())

    private fun scrollbackSize(): Int =
        history.size - realScreenLineCount()

    private fun blankScreenLineCount(): Int =
        height - realScreenLineCount()

    private fun screenStartIndex(): Int =
        scrollbackSize()

    private fun screenLines(): List<Line> {
        val result = mutableListOf<Line>()

        repeat(blankScreenLineCount()) {
            result.add(blankLine())
        }

        for (i in screenStartIndex() until history.size) {
            result.add(history.getLine(i))
        }

        return result
    }

    private fun blankLine(): Line =
        Line(MutableList(width) { Cell() })

}