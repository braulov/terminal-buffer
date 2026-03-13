package org.example.storage

import org.example.model.Cell
import org.example.model.Line
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

abstract class ScreenBufferContractTest {

    protected abstract fun createScreenBuffer(width: Int, height: Int): ScreenBuffer

    @Test
    fun createsRequiredNumberOfBlankLines() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertEquals(5, screen.width)
        assertEquals(3, screen.height)
        assertEquals(listOf("     ", "     ", "     "), screen.lines().map { it.asPlainString() })
    }

    @Test
    fun setLineReplacesLineAtRow() {
        val screen = createScreenBuffer(width = 5, height = 3)
        val line = lineOf("abc", 5)

        screen.setLine(1, line)

        assertSame(line, screen.getLine(1))
        assertEquals("abc  ", screen.getLine(1).asPlainString())
    }

    @Test
    fun pushBottomRemovesTopLineAndAppendsNewBottomLine() {
        val screen = createScreenBuffer(width = 5, height = 3)

        val first = lineOf("111", 5)
        val second = lineOf("222", 5)
        val third = lineOf("333", 5)
        val newBottom = lineOf("999", 5)

        screen.setLine(0, first)
        screen.setLine(1, second)
        screen.setLine(2, third)

        val removed = screen.pushBottom(newBottom)

        assertSame(first, removed)
        assertEquals(listOf("222  ", "333  ", "999  "), screen.lines().map { it.asPlainString() })
    }

    @Test
    fun pushBottomCanBeCalledRepeatedly() {
        val screen = createScreenBuffer(width = 3, height = 2)

        screen.setLine(0, lineOf("A", 3))
        screen.setLine(1, lineOf("B", 3))

        screen.pushBottom(lineOf("C", 3))
        screen.pushBottom(lineOf("D", 3))

        assertEquals(listOf("C  ", "D  "), screen.lines().map { it.asPlainString() })
    }

    @Test
    fun clearResetsAllLinesToBlank() {
        val screen = createScreenBuffer(width = 4, height = 2)

        screen.setLine(0, lineOf("ab", 4))
        screen.setLine(1, lineOf("cd", 4))

        screen.clear()

        assertEquals(listOf("    ", "    "), screen.lines().map { it.asPlainString() })
    }

    @Test
    fun clearPreservesScreenDimensions() {
        val screen = createScreenBuffer(width = 4, height = 3)

        screen.setLine(0, lineOf("ab", 4))
        screen.setLine(1, lineOf("cd", 4))
        screen.setLine(2, lineOf("ef", 4))

        screen.clear()

        assertEquals(4, screen.width)
        assertEquals(3, screen.height)
        assertEquals(3, screen.lines().size)
        assertEquals(listOf("    ", "    ", "    "), screen.lines().map { it.asPlainString() })
    }

    @Test
    fun getLineFailsForNegativeRow() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertFailsWith<IllegalArgumentException> {
            screen.getLine(-1)
        }
    }

    @Test
    fun getLineFailsForRowPastHeight() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertFailsWith<IllegalArgumentException> {
            screen.getLine(3)
        }
    }

    @Test
    fun setLineFailsForNegativeRow() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertFailsWith<IllegalArgumentException> {
            screen.setLine(-1, lineOf("abc", 5))
        }
    }

    @Test
    fun setLineFailsForRowPastHeight() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertFailsWith<IllegalArgumentException> {
            screen.setLine(3, lineOf("abc", 5))
        }
    }

    @Test
    fun setLineFailsForWrongLineWidth() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertFailsWith<IllegalArgumentException> {
            screen.setLine(1, lineOf("abc", 4))
        }
    }

    @Test
    fun pushBottomFailsForWrongLineWidth() {
        val screen = createScreenBuffer(width = 5, height = 3)

        assertFailsWith<IllegalArgumentException> {
            screen.pushBottom(lineOf("abc", 4))
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