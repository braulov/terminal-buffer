package org.example

import org.example.model.Attributes
import org.example.model.Cell
import org.example.model.Cursor
import org.example.model.Line
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

    fun writeText(text: String): Nothing = TODO()
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

    // Temporary helper for tests before writeText is implemented.
    fun appendLineForTest(text: String) {
        consumeOneVirtualBlankRowIfPresent()
        history.appendLine(lineOf(text))
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