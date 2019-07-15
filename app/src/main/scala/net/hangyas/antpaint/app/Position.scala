package net.hangyas.antpaint.app

/**
 * Created by hangyas on 2015-06-05
 */
case class Position(x: Float, y: Float) {
  def - (other: Position) = Position(x - other.x, y - other.y)
  def + (other: Position) = Position(x + other.x, y + other.y)
  def - (other: PositionI) = Position(x - other.x, y - other.y)
  def + (other: PositionI) = Position(x + other.x, y + other.y)
  def / (submultiple: Float) = Position(x / submultiple, y / submultiple)
  def * (multiple: Float) = Position(x * multiple, y * multiple)

  def left = Position(-y, x)
  def right = Position(y, -x)

  def abs: Int = Math.sqrt(x * x + y * y).toInt
  override def clone: Position = Position(x, y)
}

/**
 * vízfesték folyatásához
 * */
case class PositionI(x: Int, y: Int) {

  def this(x: Float, y: Float) = this(x.toInt, y.toInt)
  def this(p: Position) = this(p.x.toInt, p.y.toInt)

  def - (other: Position) = Position(x - other.x, y - other.y)
  def + (other: Position) = Position(x + other.x, y + other.y)
  def - (other: PositionI) = PositionI(x - other.x, y - other.y)
  def + (other: PositionI) = PositionI(x + other.x, y + other.y)
  def / (submultiple: Int) = PositionI(x / submultiple, y / submultiple)

  def abs(): Int = Math.sqrt(x * x + y * y).toInt
  override def clone: Position = Position(x, y)
}