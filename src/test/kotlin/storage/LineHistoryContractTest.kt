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
    fun initializesWithExactlyScreenHeightBlankLines() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)

        assertEquals(5, history.width)
        assertEquals(3, history.height)
        assertEquals(10, history.scrollbackMaxSize)
        assertEquals(3, history.size)
        assertEquals(listOf("     ", "     ", "     "), history.lines().map { it.asPlainString() })
    }

    @Test
    fun setLineReplacesLineAtGlobalIndex() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)
        val line = lineOf("abc", 5)

        history.setLine(1, line)

        assertSame(line, history.getLine(1))
        assertEquals("abc  ", history.getLine(1).asPlainString())
    }

    @Test
    fun appendLineAddsLineAtBottomOfVisibleArea() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)

        history.setLine(0, lineOf("111", 5))
        history.setLine(1, lineOf("222", 5))
        history.setLine(2, lineOf("333", 5))

        history.appendLine(lineOf("999", 5))

        assertEquals(4, history.size)
        assertEquals(
            listOf("111  ", "222  ", "333  ", "999  "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun appendLineTurnsOldTopScreenLinesIntoScrollback() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 10)

        history.setLine(0, lineOf("111", 5))
        history.setLine(1, lineOf("222", 5))
        history.setLine(2, lineOf("333", 5))

        history.appendLine(lineOf("444", 5))
        history.appendLine(lineOf("555", 5))

        assertEquals(
            listOf("111  ", "222  ", "333  ", "444  ", "555  "),
            history.lines().map { it.asPlainString() }
        )
    }

    @Test
    fun dropsOldestLineWhenCapacityIsExceeded() {
        val history = createLineHistory(width = 5, height = 2, scrollbackMaxSize = 2)
        // capacity = 4

        history.setLine(0, lineOf("A", 5))
        history.setLine(1, lineOf("B", 5))

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
    fun keepsOnlyVisibleScreenWhenScrollbackSizeIsZero() {
        val history = createLineHistory(width = 4, height = 2, scrollbackMaxSize = 0)

        history.setLine(0, lineOf("A", 4))
        history.setLine(1, lineOf("B", 4))

        history.appendLine(lineOf("C", 4))
        history.appendLine(lineOf("D", 4))

        assertEquals(2, history.size)
        assertEquals(listOf("C   ", "D   "), history.lines().map { it.asPlainString() })
    }

    @Test
    fun clearResetsToExactlyBlankScreenHeightLines() {
        val history = createLineHistory(width = 4, height = 3, scrollbackMaxSize = 2)

        history.setLine(0, lineOf("A", 4))
        history.setLine(1, lineOf("B", 4))
        history.setLine(2, lineOf("C", 4))
        history.appendLine(lineOf("D", 4))

        history.clear()

        assertEquals(3, history.size)
        assertEquals(listOf("    ", "    ", "    "), history.lines().map { it.asPlainString() })
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

        assertFailsWith<IllegalArgumentException> {
            history.getLine(3)
        }
    }

    @Test
    fun setLineFailsForNegativeIndex() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)

        assertFailsWith<IllegalArgumentException> {
            history.setLine(-1, lineOf("abc", 5))
        }
    }

    @Test
    fun setLineFailsForIndexPastSize() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)

        assertFailsWith<IllegalArgumentException> {
            history.setLine(3, lineOf("abc", 5))
        }
    }

    @Test
    fun setLineFailsForWrongWidth() {
        val history = createLineHistory(width = 5, height = 3, scrollbackMaxSize = 2)

        assertFailsWith<IllegalArgumentException> {
            history.setLine(1, lineOf("abc", 4))
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