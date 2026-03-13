package org.example

import org.example.model.MoveType
import org.example.model.Position
import kotlin.test.Test
import kotlin.test.assertEquals

class TerminalBufferTest {

    @Test
    fun writeTextWritesIntoCurrentLine() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeText("abc")

        assertEquals(
            "abc  \n     ",
            buffer.getScreen()
        )
    }

    @Test
    fun writeTextRespectsCursorPosition() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.moveCursor(MoveType.RIGHT, 2)
        buffer.writeText("xy")

        assertEquals(
            "  xy \n     ",
            buffer.getScreen()
        )
    }

    @Test
    fun writeTextOverridesExistingCells() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeText("hello")
        buffer.setCursorPosition(Position(1, 0))
        buffer.writeText("X")

        assertEquals(
            "hXllo\n     ",
            buffer.getScreen()
        )
    }

    @Test
    fun writeTextClipsAtLineEnd() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.setCursorPosition(Position(3, 0))
        buffer.writeText("abcdef")

        assertEquals(
            "   ab\n     ",
            buffer.getScreen()
        )
    }

    @Test
    fun writeTextMaterializesVirtualRow() {
        val buffer = TerminalBuffer(5, 3, 10)

        buffer.setCursorPosition(Position(0, 2))
        buffer.writeText("abc")

        assertEquals(
            "     \n     \nabc  ",
            buffer.getScreen()
        )
    }
}