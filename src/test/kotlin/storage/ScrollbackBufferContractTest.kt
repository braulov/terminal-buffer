package org.example.storage

import org.example.model.Cell
import org.example.model.Line
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class ScrollbackBufferContractTest {

    protected abstract fun createScrollbackBuffer(maxSize: Int): ScrollbackBuffer

    @Test
    fun storesLinesInInsertionOrder() {
        val scrollback = createScrollbackBuffer(maxSize = 5)

        scrollback.addLine(lineOf("111", 5))
        scrollback.addLine(lineOf("222", 5))
        scrollback.addLine(lineOf("333", 5))

        assertEquals(3, scrollback.size)
        assertEquals("111  ", scrollback.getLine(0).asPlainString())
        assertEquals("222  ", scrollback.getLine(1).asPlainString())
        assertEquals("333  ", scrollback.getLine(2).asPlainString())
    }

    @Test
    fun dropsOldestLineWhenMaxSizeIsExceeded() {
        val scrollback = createScrollbackBuffer(maxSize = 2)

        scrollback.addLine(lineOf("111", 5))
        scrollback.addLine(lineOf("222", 5))
        scrollback.addLine(lineOf("333", 5))

        assertEquals(2, scrollback.size)
        assertEquals("222  ", scrollback.getLine(0).asPlainString())
        assertEquals("333  ", scrollback.getLine(1).asPlainString())
    }

    @Test
    fun preservesLastMaxSizeLinesAfterMultipleOverflows() {
        val scrollback = createScrollbackBuffer(maxSize = 3)

        scrollback.addLine(lineOf("A", 3))
        scrollback.addLine(lineOf("B", 3))
        scrollback.addLine(lineOf("C", 3))
        scrollback.addLine(lineOf("D", 3))
        scrollback.addLine(lineOf("E", 3))

        assertEquals(3, scrollback.size)
        assertEquals(
            listOf("C  ", "D  ", "E  "),
            scrollback.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun clearRemovesAllLines() {
        val scrollback = createScrollbackBuffer(maxSize = 3)

        scrollback.addLine(lineOf("111", 5))
        scrollback.addLine(lineOf("222", 5))

        scrollback.clear()

        assertEquals(0, scrollback.size)
        assertEquals(emptyList(), scrollback.lines())
    }

    @Test
    fun ignoresAddedLinesWhenMaxSizeIsZero() {
        val scrollback = createScrollbackBuffer(maxSize = 0)

        scrollback.addLine(lineOf("111", 5))
        scrollback.addLine(lineOf("222", 5))

        assertEquals(0, scrollback.size)
        assertEquals(emptyList(), scrollback.lines())
    }

    @Test
    fun linesReturnsLinesInLogicalOrder() {
        val scrollback = createScrollbackBuffer(maxSize = 3)

        scrollback.addLine(lineOf("A", 3))
        scrollback.addLine(lineOf("B", 3))
        scrollback.addLine(lineOf("C", 3))

        assertEquals(
            listOf("A  ", "B  ", "C  "),
            scrollback.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun getLineFailsForNegativeIndex() {
        val scrollback = createScrollbackBuffer(maxSize = 3)

        assertFailsWith<IllegalArgumentException> {
            scrollback.getLine(-1)
        }
    }

    @Test
    fun getLineFailsForIndexPastSize() {
        val scrollback = createScrollbackBuffer(maxSize = 3)
        scrollback.addLine(lineOf("A", 3))

        assertFailsWith<IllegalArgumentException> {
            scrollback.getLine(1)
        }
    }

    @Test
    fun clearCanBeCalledOnEmptyBuffer() {
        val scrollback = createScrollbackBuffer(maxSize = 3)

        scrollback.clear()

        assertEquals(0, scrollback.size)
        assertEquals(emptyList(), scrollback.lines())
    }

    protected fun lineOf(text: String, width: Int): Line {
        val cells = MutableList(width) { Cell() }
        for (i in text.indices) {
            if (i >= width) break
            cells[i] = Cell(text[i])
        }
        return Line(cells)
    }
}