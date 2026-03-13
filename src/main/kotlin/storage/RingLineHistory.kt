package org.example.storage

import org.example.model.Line

class RingLineHistory(
    override val width: Int,
    override val height: Int,
    override val scrollbackMaxSize: Int
) : LineHistory {

    init {
        require(width > 0) { "Width must be positive" }
        require(height > 0) { "Height must be positive" }
        require(scrollbackMaxSize >= 0) { "Scrollback max size must be non-negative" }
    }

    private val capacity: Int = height + scrollbackMaxSize
    private val buffer: Array<Line?> = arrayOfNulls(capacity)

    private var head: Int = 0

    override var size: Int = 0
        private set

    override fun getLine(globalRow: Int): Line {
        requireValidIndex(globalRow)
        return buffer[physicalIndex(globalRow)]!!
    }

    override fun setLine(globalRow: Int, line: Line) {
        requireValidIndex(globalRow)
        require(line.cells.size == width) {
            "Line width ${line.cells.size} does not match history width $width"
        }

        buffer[physicalIndex(globalRow)] = line
    }

    override fun appendLine(line: Line) {
        require(line.cells.size == width) {
            "Line width ${line.cells.size} does not match history width $width"
        }

        if (size < capacity) {
            buffer[physicalIndex(size)] = line
            size++
        } else {
            buffer[head] = line
            head = (head + 1) % capacity
        }
    }

    override fun clear() {
        for (i in buffer.indices) {
            buffer[i] = null
        }
        head = 0
        size = 0
    }

    override fun lines(): List<Line> {
        return List(size) { index -> getLine(index) }
    }

    private fun requireValidIndex(index: Int) {
        require(index in 0 until size) {
            "Index $index is out of bounds for size $size"
        }
    }

    private fun physicalIndex(logicalIndex: Int): Int {
        return (head + logicalIndex) % capacity
    }
}