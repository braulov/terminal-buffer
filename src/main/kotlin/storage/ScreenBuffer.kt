package org.example.storage

import org.example.model.Line

interface ScreenBuffer {
    val height: Int
    val width: Int

    fun getLine(row: Int): Line
    fun setLine(row: Int, line: Line)

    /**
     * Removes the top visible line and appends [newBottomLine] at the bottom.
     * Returns the removed top line.
     */
    fun pushBottom(newBottomLine: Line): Line

    fun clear()
    fun lines(): List<Line>
}