package org.example.storage

import org.example.model.Line

interface LineHistory {
    val width: Int
    val height: Int
    val scrollbackMaxSize: Int
    val size: Int

    fun getLine(globalRow: Int): Line
    fun setLine(globalRow: Int, line: Line)
    fun appendLine(line: Line)
    fun clear()
    fun lines(): List<Line>
}