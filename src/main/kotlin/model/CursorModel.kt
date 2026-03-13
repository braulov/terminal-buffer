package org.example.model

class Cursor(
    private var position: Position = Position(0,0)
) {
    fun getPosition(): Position = position
    fun setPosition(position: Position) {this.position = position}
    fun move (type: MoveType, count: Int = 1) {position.move(type, count)}
}

data class Position(var column: Int, var row: Int) {
    fun move(type: MoveType, count: Int = 1) {
        when (type) {
            MoveType.UP -> row -= count
            MoveType.DOWN -> row += count
            MoveType.LEFT -> column -= count
            MoveType.RIGHT -> column += count
        }
        // todo: bound exception
    }
}

enum class MoveType {
    UP,DOWN,LEFT,RIGHT
}

