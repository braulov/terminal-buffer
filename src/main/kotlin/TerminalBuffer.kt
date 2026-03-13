package org.example

import org.example.model.Attributes
import org.example.model.Position
import org.example.model.Cursor
import org.example.storage.ScreenBuffer
import org.example.storage.ScrollbackBuffer

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
    private var screen: ScreenBuffer = TODO()
    private var scrollback: ScrollbackBuffer = TODO()
    fun setAttributes(
        foregroundColor: Int,
        backgroundColor: Int,
        style: Int
    ) {
        attributes = Attributes(
            foregroundColor,
            backgroundColor,
            style
        )
    }

    fun writeText(text: String): Nothing = TODO()
    fun insertText(text: String): Nothing = TODO()
    fun fillLine(row: Int, char: Char?): Nothing = TODO()

    fun insertEmptyLineAtBottom(): Nothing = TODO()
    fun clearScreen(): Nothing = TODO()
    fun clearScreenAndScrollback(): Nothing = TODO()

    fun getChar(position: Position): Char? = TODO()
    fun getAttributes(position: Position): Attributes = TODO()
    fun getLine(row: Int): String = TODO()
    fun getScreen(): String = TODO()
    fun getScreenAndScrollback(): String = TODO()
    fun resize(width: Int, height: Int): Nothing = TODO()
}
