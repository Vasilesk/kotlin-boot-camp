package io.rybalkinsd.kotlinbootcamp.geometry

/**
 * Entity that can physically intersect, like flame and player
 */
interface Collider {
    fun isColliding(other: Collider): Boolean
    fun containsPoint(x: Int, y: Int): Boolean
    fun getFirstCornerX(): Int
    fun getFirstCornerY(): Int
    fun getSecondCornerX(): Int
    fun getSecondCornerY(): Int
}

/**
 * 2D point with integer coordinates
 */
class Point(x: Int, y: Int) : Collider {
    val x = x
    val y = y

    override fun equals(other: Any?): Boolean = other is Point && this.x == other.x && this.y == other.y

    override fun getFirstCornerX(): Int = this.x
    override fun getFirstCornerY(): Int = this.y
    override fun getSecondCornerX(): Int = this.x
    override fun getSecondCornerY(): Int = this.y

    override fun isColliding(other: Collider): Boolean = other.containsPoint(this.x, this.y)

    override fun containsPoint(x: Int, y: Int): Boolean = this.x == x && this.y == y
}

/**
 * Bar is a rectangle, which borders are parallel to coordinate axis
 * Like selection bar in desktop, this bar is defined by two opposite corners
 * Bar is not oriented
 * (It does not matter, which opposite corners you choose to define bar)
 */
class Bar(firstCornerX: Int, firstCornerY: Int, secondCornerX: Int, secondCornerY: Int) : Collider {
    val fCornerX: Int
    val fCornerY: Int
    val sCornerX: Int
    val sCornerY: Int

    init {
        fCornerX = minOf(firstCornerX, secondCornerX)
        sCornerX = maxOf(firstCornerX, secondCornerX)
        fCornerY = minOf(firstCornerY, secondCornerY)
        sCornerY = maxOf(firstCornerY, secondCornerY)
    }

    override fun equals(other: Any?): Boolean = other is Bar &&
            this.fCornerX == other.fCornerX &&
            this.fCornerY == other.fCornerY &&
            this.sCornerX == other.sCornerX &&
            this.sCornerY == other.sCornerY

    override fun getFirstCornerX(): Int = this.fCornerX
    override fun getFirstCornerY(): Int = this.fCornerY
    override fun getSecondCornerX(): Int = this.sCornerX
    override fun getSecondCornerY(): Int = this.sCornerY

    override fun isColliding(other: Collider): Boolean = other.containsPoint(this.fCornerX, this.fCornerY) ||
            other.containsPoint(this.sCornerX, this.sCornerY) ||
            other.containsPoint(this.fCornerX, this.sCornerY) ||
            other.containsPoint(this.sCornerX, this.fCornerY) ||
            this.containsPoint(other.getFirstCornerX(), other.getFirstCornerY()) ||
            this.containsPoint(other.getSecondCornerX(), other.getSecondCornerY()) ||
            this.containsPoint(other.getFirstCornerX(), other.getSecondCornerY()) ||
            this.containsPoint(other.getSecondCornerX(), other.getFirstCornerY())

    override fun containsPoint(x: Int, y: Int): Boolean = (this.fCornerX <= x).xor(this.sCornerX <= x) && (this.fCornerY <= y).xor(this.sCornerY <= y)
}