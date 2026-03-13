package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class TerminalBufferTest {

    @Test
    fun initialScreenIsBlank() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        assertEquals(
            "     \n     \n     ",
            buffer.getScreen()
        )
        assertEquals(
            "     \n     \n     ",
            buffer.getScreenAndScrollback()
        )
    }

    @Test
    fun screenShowsRealLinesAtBottomWhenHistoryIsSmallerThanHeight() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        buffer.appendLineForTest("abc")
        buffer.appendLineForTest("de")

        assertEquals(
            "     \nabc  \nde   ",
            buffer.getScreen()
        )

        assertEquals("     ", buffer.getLine(0))
        assertEquals("abc  ", buffer.getLine(1))
        assertEquals("de   ", buffer.getLine(2))
    }

    @Test
    fun screenShowsLastHeightLinesAndScrollbackShowsOlderLines() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        buffer.appendLineForTest("111")
        buffer.appendLineForTest("222")
        buffer.appendLineForTest("333")
        buffer.appendLineForTest("444")
        buffer.appendLineForTest("555")

        assertEquals(
            "333  \n444  \n555  ",
            buffer.getScreen()
        )

        assertEquals(
            "111  \n222  \n333  \n444  \n555  ",
            buffer.getScreenAndScrollback()
        )

        assertEquals("111  ", buffer.getLine(0))
        assertEquals("222  ", buffer.getLine(1))
        assertEquals("333  ", buffer.getLine(2))
        assertEquals("444  ", buffer.getLine(3))
        assertEquals("555  ", buffer.getLine(4))
    }

    @Test
    fun insertEmptyLineAtBottomAddsRealBlankLineToHistory() {
        val buffer = TerminalBuffer(width = 4, height = 3, scrollbackMaxSize = 10)

        buffer.appendLineForTest("AA")
        buffer.appendLineForTest("BB")
        buffer.insertEmptyLineAtBottom()

        assertEquals(
            "AA  \nBB  \n    ",
            buffer.getScreen()
        )

        assertEquals(
            "AA  \nBB  \n    ",
            buffer.getScreenAndScrollback()
        )
    }

    @Test
    fun insertEmptyLineAtBottomMovesTopVisibleLineIntoScrollback() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.appendLineForTest("AA")
        buffer.appendLineForTest("BB")
        buffer.insertEmptyLineAtBottom()

        assertEquals(
            "BB  \n    ",
            buffer.getScreen()
        )

        assertEquals(
            "AA  \nBB  \n    ",
            buffer.getScreenAndScrollback()
        )
    }

    @Test
    fun scrollbackOverflowDropsOldestLine() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 2)

        buffer.appendLineForTest("A")
        buffer.appendLineForTest("B")
        buffer.appendLineForTest("C")
        buffer.appendLineForTest("D")
        buffer.appendLineForTest("E")

        assertEquals(
            "D   \nE   ",
            buffer.getScreen()
        )

        assertEquals(
            "B   \nC   \nD   \nE   ",
            buffer.getScreenAndScrollback()
        )
    }

    @Test
    fun clearScreenRemovesVisibleRealLinesButPreservesScrollback() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.appendLineForTest("A")
        buffer.appendLineForTest("B")
        buffer.appendLineForTest("C")
        buffer.appendLineForTest("D")

        buffer.clearScreen()

        assertEquals(
            "    \n    ",
            buffer.getScreen()
        )

        assertEquals(
            "A   \nB   \n    \n    ",
            buffer.getScreenAndScrollback()
        )
    }

    @Test
    fun clearScreenAndScrollbackRemovesAllRealLines() {
        val buffer = TerminalBuffer(width = 4, height = 3, scrollbackMaxSize = 10)

        buffer.appendLineForTest("A")
        buffer.appendLineForTest("B")
        buffer.appendLineForTest("C")
        buffer.appendLineForTest("D")

        buffer.clearScreenAndScrollback()

        assertEquals(
            "    \n    \n    ",
            buffer.getScreen()
        )

        assertEquals(
            "    \n    \n    ",
            buffer.getScreenAndScrollback()
        )
    }

    @Test
    fun appendingLineAfterClearScreenConsumesVirtualBlankRow() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.appendLineForTest("A")
        buffer.appendLineForTest("B")
        buffer.appendLineForTest("C")

        buffer.clearScreen()
        buffer.appendLineForTest("X")

        assertEquals(
            "    \nX   ",
            buffer.getScreen()
        )

        assertEquals(
            "A   \n    \nX   ",
            buffer.getScreenAndScrollback()
        )
    }
}