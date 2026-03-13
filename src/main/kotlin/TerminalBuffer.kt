package org.example

import org.example.model.Attributes
import org.example.model.Cell
import org.example.model.Cursor
import org.example.model.Line
import org.example.model.MoveType
import org.example.model.Position
import org.example.storage.LineHistory
import org.example.storage.RingLineHistory

class TerminalBuffer(
    private var width: Int,
    private var height: Int,
    private var scrollbackMaxSize: Int
) {
    init {
        require(width > 0) { "Width must be positive" }
        require(height > 0) { "Height must be positive" }
        require(scrollbackMaxSize >= 0) { "Scrollback max size must be non-negative" }
    }

    private var attributes: Attributes = Attributes()
    private var cursor: Cursor = Cursor()
    private var history: LineHistory = RingLineHistory(width, height, scrollbackMaxSize)
    private var virtualBlankScreenRows: Int = 0

    fun setAttributes(
        foregroundColor: Int,
        backgroundColor: Int,
        style: Int
    ) {
        attributes = Attributes(foregroundColor, backgroundColor, style)
    }

    fun getCursorPosition(): Position = cursor.getPosition()

    fun setCursorPosition(position: Position) {
        val clampedColumn = position.column.coerceIn(0, width - 1)
        val clampedRow = position.row.coerceIn(0, height - 1)
        cursor.setPosition(Position(clampedColumn, clampedRow))
    }

    fun moveCursor(type: MoveType, count: Int = 1) {
        require(count >= 0) { "Count must be non-negative" }

        val current = cursor.getPosition()
        val next = when (type) {
            MoveType.UP -> Position(current.column, (current.row - count).coerceAtLeast(0))
            MoveType.DOWN -> Position(current.column, (current.row + count).coerceAtMost(height - 1))
            MoveType.LEFT -> Position((current.column - count).coerceAtLeast(0), current.row)
            MoveType.RIGHT -> Position((current.column + count).coerceAtMost(width - 1), current.row)
        }
        cursor.setPosition(next)
    }

    fun writeText(text: String) {
        var wrapPending = false

        for (ch in text) {
            if (ch == '\n') {
                moveToNextLine()
                wrapPending = false
                continue
            }

            if (wrapPending) {
                moveToNextLine()
                wrapPending = false
            }

            wrapPending = writeCharAndCheckWrap(ch)
        }
    }

    fun insertText(text: String): Nothing = TODO()

    fun fillLine(row: Int, char: Char?): Nothing = TODO()

    fun insertEmptyLineAtBottom() {
        consumeOneVirtualBlankRowIfPresent()
        history.appendLine(blankLine())
    }

    fun clearScreen() {
        val preservedScrollbackSize = scrollbackSize()
        val newHistory = RingLineHistory(width, height, scrollbackMaxSize)

        for (i in 0 until preservedScrollbackSize) {
            newHistory.appendLine(history.getLine(i))
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

    fun getChar(position: Position): Char? {
        require(position.column in 0 until width) {
            "Column ${position.column} is out of bounds for width $width"
        }
        val line = getLineObject(position.row)
        return line.cells[position.column].character
    }

    fun getAttributes(position: Position): Attributes {
        require(position.column in 0 until width) {
            "Column ${position.column} is out of bounds for width $width"
        }
        val line = getLineObject(position.row)
        return line.cells[position.column].attributes
    }

    fun getLine(row: Int): String {
        require(row >= 0) { "Row must be non-negative" }
        require(row < totalVisibleRows()) {
            "Row $row is out of bounds for total rows ${totalVisibleRows()}"
        }
        return getLineObject(row).asPlainString()
    }

    fun getScreen(): String {
        return screenLines().joinToString("\n") { it.asPlainString() }
    }

    fun getScreenAndScrollback(): String {
        val lines = mutableListOf<Line>()

        for (i in 0 until scrollbackSize()) {
            lines.add(history.getLine(i))
        }
        lines.addAll(screenLines())

        return lines.joinToString("\n") { it.asPlainString() }
    }

    fun resize(width: Int, height: Int): Nothing = TODO()

    fun appendLineForTest(text: String) {
        consumeOneVirtualBlankRowIfPresent()
        history.appendLine(lineOf(text))
    }

    private fun writeCharAndCheckWrap(ch: Char): Boolean {
        val pos = cursor.getPosition()
        val line = ensureWritableScreenLine(pos.row)
        line.cells[pos.column] = Cell(ch, attributes)

        return if (pos.column < width - 1) {
            cursor.setPosition(Position(pos.column + 1, pos.row))
            false
        } else {
            true
        }
    }

    private fun moveToNextLine() {
        val pos = cursor.getPosition()
        if (pos.row < height - 1) {
            cursor.setPosition(Position(0, pos.row + 1))
        } else {
            consumeOneVirtualBlankRowIfPresent()
            history.appendLine(blankLine())
            cursor.setPosition(Position(0, height - 1))
        }
    }

    private fun ensureWritableScreenLine(screenRow: Int): Line {
        require(screenRow in 0 until height)

        val blankLines = blankScreenLineCount()

        return if (screenRow < blankLines) {
            val toMaterialize = blankLines - screenRow

            repeat(toMaterialize) {
                consumeOneVirtualBlankRowIfPresent()
                history.appendLine(blankLine())
            }

            history.getLine(history.size - realScreenLineCount())
        } else {
            val index = screenStartIndex() + (screenRow - blankLines)
            history.getLine(index)
        }
    }

    private fun consumeOneVirtualBlankRowIfPresent() {
        if (virtualBlankScreenRows > 0) {
            virtualBlankScreenRows--
        }
    }

    private fun totalVisibleRows(): Int = scrollbackSize() + height

    private fun realScreenCapacity(): Int = height - virtualBlankScreenRows

    private fun realScreenLineCount(): Int = minOf(history.size, realScreenCapacity())

    private fun scrollbackSize(): Int = history.size - realScreenLineCount()

    private fun blankScreenLineCount(): Int = height - realScreenLineCount()

    private fun screenStartIndex(): Int = scrollbackSize()

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

    private fun getLineObject(globalRow: Int): Line {
        val scrollbackSize = scrollbackSize()

        return if (globalRow < scrollbackSize) {
            history.getLine(globalRow)
        } else {
            val screenRow = globalRow - scrollbackSize
            screenLines()[screenRow]
        }
    }

    private fun blankLine(): Line =
        Line(MutableList(width) { Cell() })

    private fun lineOf(text: String): Line {
        val cells = MutableList(width) { Cell() }
        for (i in text.indices) {
            if (i >= width) break
            cells[i] = Cell(text[i], attributes)
        }
        return Line(cells)
    }
}