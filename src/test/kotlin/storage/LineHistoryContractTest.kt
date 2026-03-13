package org.example.storage

import org.example.model.Cell
import org.example.model.Line
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

abstract class LineHistoryContractTest {

    protected abstract fun createLineHistory(
        width: Int,
        height: Int,
        scrollbackMaxSize: Int
    ): LineHistory

    @Test
    fun initializesEmpty() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)

        assertEquals(5, history.width)
        assertEquals(3, history.height)
        assertEquals(10, history.scrollbackMaxSize)
        assertEquals(0, history.size)
        assertEquals(emptyList(), history.lines())
    }

    @Test
    fun appendLineStoresLinesInInsertionOrder() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)

        history.appendLine(lineOf("111", 5))
        history.appendLine(lineOf("222", 5))
        history.appendLine(lineOf("333", 5))

        assertEquals(3, history.size)
        assertEquals(
            listOf("111  ", "222  ", "333  "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun setLineReplacesLineAtIndex() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)

        history.appendLine(lineOf("111", 5))
        history.appendLine(lineOf("222", 5))
        history.appendLine(lineOf("333", 5))

        val replacement = lineOf("abc", 5)
        history.setLine(1, replacement)

        assertSame(replacement, history.getLine(1))
        assertEquals(
            listOf("111  ", "abc  ", "333  "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun dropsOldestLineWhenCapacityIsExceeded() {
        val history = createLineHistory(width = 5, height = 2, scrollbackMaxSize = 2)
        // capacity = 4

        history.appendLine(lineOf("A", 5))
        history.appendLine(lineOf("B", 5))
        history.appendLine(lineOf("C", 5))
        history.appendLine(lineOf("D", 5))
        history.appendLine(lineOf("E", 5))

        assertEquals(4, history.size)
        assertEquals(
            listOf("B    ", "C    ", "D    ", "E    "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun keepsOnlyScreenHeightLinesWhenScrollbackSizeIsZero() {
        val history = createLineHistory(width = 4, height = 2, scrollbackMaxSize = 0)

        history.appendLine(lineOf("A", 4))
        history.appendLine(lineOf("B", 4))
        history.appendLine(lineOf("C", 4))
        history.appendLine(lineOf("D", 4))

        assertEquals(2, history.size)
        assertEquals(listOf("C   ", "D   "), history.lines().map { it.asPlainString() })
    }

    @Test
    fun linesReturnsLinesInLogicalOrderAfterWrapAround() {
        val history = createLineHistory(width = 4, height = 2, scrollbackMaxSize = 2)
        // capacity = 4

        history.appendLine(lineOf("A", 4))
        history.appendLine(lineOf("B", 4))
        history.appendLine(lineOf("C", 4))
        history.appendLine(lineOf("D", 4))
        history.appendLine(lineOf("E", 4))
        history.appendLine(lineOf("F", 4))

        assertEquals(
            listOf("C   ", "D   ", "E   ", "F   "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun getLineReturnsLogicalOrderAfterWrapAround() {
        val history = createLineHistory(width = 4, height = 2, scrollbackMaxSize = 2)

        history.appendLine(lineOf("A", 4))
        history.appendLine(lineOf("B", 4))
        history.appendLine(lineOf("C", 4))
        history.appendLine(lineOf("D", 4))
        history.appendLine(lineOf("E", 4))

        assertEquals("B   ", history.getLine(0).asPlainString())
        assertEquals("C   ", history.getLine(1).asPlainString())
        assertEquals("D   ", history.getLine(2).asPlainString())
        assertEquals("E   ", history.getLine(3).asPlainString())
    }

    @Test
    fun setLineWorksAfterWrapAround() {
        val history = createLineHistory(width = 4, height = 2, scrollbackMaxSize = 2)

        history.appendLine(lineOf("A", 4))
        history.appendLine(lineOf("B", 4))
        history.appendLine(lineOf("C", 4))
        history.appendLine(lineOf("D", 4))
        history.appendLine(lineOf("E", 4))

        val replacement = lineOf("XX", 4)
        history.setLine(1, replacement)

        assertSame(replacement, history.getLine(1))
        assertEquals(
            listOf("B   ", "XX  ", "D   ", "E   "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun clearRemovesAllLines() {
        val history = createLineHistory(width = 4, height = 3, scrollbackMaxSize = 2)

        history.appendLine(lineOf("A", 4))
        history.appendLine(lineOf("B", 4))
        history.appendLine(lineOf("C", 4))
        history.appendLine(lineOf("D", 4))

        history.clear()

        assertEquals(0, history.size)
        assertEquals(emptyList(), history.lines())
    }

    @Test
    fun clearCanBeCalledOnEmptyHistory() {
        val history = createLineHistory(width = 4, height = 2, scrollbackMaxSize = 2)

        history.clear()

        assertEquals(0, history.size)
        assertEquals(emptyList(), history.lines())
    }

    @Test
    fun getLineFailsForNegativeIndex() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)

        assertFailsWith<IllegalArgumentException> {
            history.getLine(-1)
        }
    }

    @Test
    fun getLineFailsForIndexPastSize() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)
        history.appendLine(lineOf("A", 5))

        assertFailsWith<IllegalArgumentException> {
            history.getLine(1)
        }
    }

    @Test
    fun setLineFailsForNegativeIndex() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)
        history.appendLine(lineOf("A", 5))

        assertFailsWith<IllegalArgumentException> {
            history.setLine(-1, lineOf("abc", 5))
        }
    }

    @Test
    fun setLineFailsForIndexPastSize() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)
        history.appendLine(lineOf("A", 5))

        assertFailsWith<IllegalArgumentException> {
            history.setLine(1, lineOf("abc", 5))
        }
    }

    @Test
    fun setLineFailsForWrongWidth() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)
        history.appendLine(lineOf("A", 5))

        assertFailsWith<IllegalArgumentException> {
            history.setLine(0, lineOf("abc", 4))
        }
    }

    @Test
    fun appendLineFailsForWrongWidth() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)

        assertFailsWith<IllegalArgumentException> {
            history.appendLine(lineOf("abc", 4))
        }
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