package org.example.model

data class Attributes(
    val foregroundColor: Int = 0,
    val backgroundColor: Int = 0,
    val style: Int = 0
)

class Line(
    val cells: MutableList<Cell>
) {
    fun asPlainString(): String =
        cells.joinToString("") { (it.character ?: ' ').toString() }

    companion object {
        fun blank(width: Int): Line =
            Line(MutableList(width) { Cell() })
    }
}

class Cell(
    val character: Char? = null,
    val attributes: Attributes = Attributes()
)