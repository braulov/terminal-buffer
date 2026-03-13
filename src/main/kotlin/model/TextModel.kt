package org.example.model

data class Attributes(
    val foregroundColor: Int = 0,
    val backgroundColor: Int = 0,
    val style: Int = 0
)

class Line(
    val cells: MutableList<Cell>
)

class Cell(
    val character: Char? = null,
    val attributes: Attributes = Attributes()
)