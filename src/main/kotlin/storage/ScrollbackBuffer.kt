package org.example.storage

import org.example.model.Line

interface ScrollbackBuffer {
    val maxSize: Int
    val size: Int

    fun addLine(line: Line)
    fun getLine(index: Int): Line
    fun clear()
    fun lines(): List<Line>
}