package net.hangyas.antpaint.app

import android.app.Notification.Style
import android.graphics.{Canvas, Color, Paint}
import android.util.Log

import scala.util.Random

/**
 * Created by hangyas on 2015-06-05
 */
abstract class Tool() {
  val paint: Paint = new Paint(Paint.ANTI_ALIAS_FLAG)
  paint.setStyle(Paint.Style.STROKE)

  def setColor(color: Int) = {
    paint.setColor(color)
  }

  def getColor = paint.getColor;

  def start(point: Position)(implicit ant: AntCanvas): Unit = {}
  def move(point: Position)(implicit ant: AntCanvas): Unit = {
    ant.canvas.drawLine(ant.lastPoint.x, ant.lastPoint.y, point.x, point.y, paint);
  }
  def end(point: Position)(implicit ant: AntCanvas): Unit = {}
}

object Tool{
  val rand = new Random()
}

object FeltPen extends Tool() {
  paint.setStrokeWidth(10)

  val circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG)
  circlePaint.setStyle(Paint.Style.FILL)

  override def setColor(color: Int) = {
    super.setColor(color)
    circlePaint.setColor(color)
  }

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    super.move(point)
    ant.canvas.drawCircle(point.x, point.y, paint.getStrokeWidth / 2, circlePaint)
  }

}


object Eraser extends Tool() {
  paint.setStrokeWidth(40)
  paint.setColor(AntCanvas.bgPaint.getColor)

  val circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG)
  circlePaint.setColor(AntCanvas.bgPaint.getColor)
  circlePaint.setStyle(Paint.Style.FILL)

  override def setColor(color: Int) = {}

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    super.move(point)
    ant.canvas.drawCircle(point.x, point.y, paint.getStrokeWidth / 2, circlePaint)
  }

  override def end(point: Position)(implicit ant: AntCanvas): Unit = {
    ant.drawingPoints = ant.drawingPoints.filter((p) => ant.bitmap.getPixel(p.x.toInt, p.y.toInt) != AntCanvas.bgPaint.getColor)
  }
}

object Pen extends Tool(){

  val randPaint = new Paint(Paint.ANTI_ALIAS_FLAG)

  override def setColor(color: Int): Unit = {
    super.setColor(color)
    randPaint.setColor(color & 0x00ffffff)
  }

  override def start(point: Position)(implicit ant: AntCanvas): Unit = {
    ant.drawingPoints += point
  }

  override def move(point: Position)(implicit ant: AntCanvas) = {
    if (ant.inner(point))
      ant.drawingPoints += point
    ant.canvas.drawLine(ant.lastPoint.x, ant.lastPoint.y, point.x, point.y, paint);

    for (i <- ant.drawingPoints){
      var t: Float = (i - point).abs / (ant.getWidth() / 2.5f);
      if (t < 1){
        t = 1 - t
       // t *= t
        randPaint.setColor((paint.getColor() & 0x00ffffff) | ((t * 55).toInt << 24))
        ant.canvas.drawLine(i.x, i.y, point.x, point.y, randPaint)
      }
    }
  }
}

object PaintBukkit extends Tool{

  override def start(point: Position)(implicit ant: AntCanvas) = {
    val p = Position(point.x, point.y)
    ant.waterPaint.start(p, paint.getColor)
  }

  override def move(point: Position)(implicit ant: AntCanvas) = {}

  override def end(point: Position)(implicit ant: AntCanvas): Unit = {
    ant.waterPaint.stop()
  }
}

object Scribble extends Tool{
  var lineEnd: Position = null //a arjzolt vonal vége
  var lastPos: Position = null //cursors helyzet, tárolunk sajátot h a legutobbi elég messze lehessen
  var irany = true
  var size = 2

  paint.setStrokeWidth(2)

  override def start(point: Position)(implicit ant: AntCanvas) = {
    lastPos = point.clone
    lineEnd = point.clone
  }

  val scrabbleLength = 5

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    var vec: Position = lastPos - point

    //nem rajzolunk ha nem húzta elég messzire
    if (vec.abs < scrabbleLength * 4){
      return
    }

    //hány részre osztjuk a vonalat
    val count: Int = vec.abs / scrabbleLength
    vec = (vec * -1) / count

    var p = lastPos.clone
    for (i <- 0 until count){
      p += vec
      var lineTo = if (irany) vec.left else vec.right
      lineTo = p + (lineTo * (Tool.rand.nextFloat() * 3 + 2))
      irany = !irany

      ant.canvas.drawLine(lineEnd.x, lineEnd.y, lineTo.x, lineTo.y, paint)

      lineEnd = lineTo
    }

    lastPos = point
  }
}

object Spray extends Tool() {
  paint.setStyle(Paint.Style.FILL)

  override def setColor(color: Int) = {
    paint.setColor(color)
    paint.setAlpha(50)
  }

  override def start(point: Position)(implicit ant: AntCanvas): Unit = {
    move(point)
  }

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    for(i <- 0 to 8){
      val p = point.clone + Position(Tool.rand.nextFloat() * 20 - 10, Tool.rand.nextFloat() * 20 - 10)
      ant.canvas.drawCircle(p.x, p.y, Tool.rand.nextFloat() * 6  + 2, paint)
    }
  }
}

object Wave extends Tool {
  var lastPos: Position = null
  var n: Float = 0

  override def setColor(color: Int) = {
    paint.setColor(color)
    paint.setAlpha(50)
  }

  override def start(point: Position)(implicit ant: AntCanvas) = {
    lastPos = point.clone
  }

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    var vec: Position = point - lastPos

    //nem rajzolunk ha nem húzta elég messzire
    if (vec.abs < 3) {
      return
    }

    val count: Int = vec.abs
    vec = vec / count

    var p = lastPos.clone
    for (i <- 0 until count) {
      p += vec

      val a = Math.sin(n).toFloat * 10
      val b = Math.cos(n).toFloat * 10
      ant.canvas.drawLine(p.x + a, p.y + b, p.x - a, p.y - b, paint);

      n += 0.1f
    }

    lastPos = point
  }
}

object Chain extends Tool{
  var lastPos: Position = null

  override def start(point: Position)(implicit ant: AntCanvas) = {
    lastPos = point.clone
  }

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    var vec: Position = lastPos - point

    //nem rajzolunk ha nem húzta elég messzire
    if (vec.abs < 5){
      return
    }

    vec = vec / 2
    var p = point.clone
    p += vec
    val a = p + vec.right
    val b = p + vec.left

    ant.canvas.drawLine(lastPos.x, lastPos.y, a.x, a.y, paint)
    ant.canvas.drawLine(point.x, point.y, a.x, a.y, paint)
    ant.canvas.drawLine(lastPos.x, lastPos.y, b.x, b.y, paint)
    ant.canvas.drawLine(point.x, point.y, b.x, b.y, paint)

    lastPos = point
  }
}

object Grid extends Tool{
  var lastPos: Position = null
  paint.setStyle(Paint.Style.FILL)

  override def start(point: Position)(implicit ant: AntCanvas) = {
    lastPos = point.clone
  }

  override def move(point: Position)(implicit ant: AntCanvas): Unit = {
    var vec: Position = lastPos - point

    //nem rajzolunk ha nem húzta elég messzire
    if (vec.abs < 5){
      return
    }

    ant.canvas.drawRect(lastPos.x, lastPos.y, point.x, point.y, paint)

    lastPos = point
  }
}