package net.hangyas.antpaint.app

import android.content.Context
import android.graphics._
import android.os.Handler
import android.util.{Log, AttributeSet}
import android.view.{MotionEvent, View}

import scala.collection.mutable

/**
 * Created by hangyas on 2015-06-05
 */

class AntCanvas(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  implicit val antCanvas = this

  var bitmap: Bitmap = null
  var canvas: Canvas = new Canvas()
  var drawingPoints = mutable.MutableList[Position]()

  def drawingPointsArray = drawingPoints.toArray

  /**
   * vízfesték folyasahoz + animálásához, időnként újrafestünk
   * */
  val handler: Handler = new Handler();
  val repaintTick = new Runnable() {
    override def run() = {
      invalidate()
      MainActivity.handleGuiShower()
      handler.postDelayed(this, 20); // 20ms == 60fps
    }
  }

  var waterPaint: WaterPaint = null
  var tool: Tool = Pen

  var activeColor: Int = Color.BLACK

  var lastPoint: Position = null

  /**
   * igazabol ez inditja el
   * */
  protected override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = init()

  var initialized = false

  def init() {
    bitmap = Bitmap.createBitmap(getWidth, getHeight, Bitmap.Config.ARGB_8888)
    canvas.setBitmap(bitmap)
    canvas.drawRect(0, 0, getWidth, getHeight, AntCanvas.bgPaint)

    waterPaint = new WaterPaint(getWidth, getHeight, this)

    handler.post(repaintTick)

    HistoryManager.forcePush

    if(savedBitmap != null){
      drawSaved(savedBitmap, savedPoints)
      savedBitmap = null
      savedPoints = null
    }

    initialized = true
  }

  /**
   * kirajzolja a betöltött .ac filet
   * ha még nem initalizálódott a felület akkor csak elmenti és majd ki lesz rajzolva ha betöltött
   * */
  def drawSaved(input: Bitmap, points: Array[Position] = null): Unit = {
    Log.v("asd", "" + input.getWidth);
    Log.v("asd", "" + input.getHeight)
    //ha még nem áll készen a canvas akkor elmentjük és majd újra betölti
    if (bitmap == null){
      savedBitmap = input
      savedPoints = points
      return
    }

    val scale = if (getWidth.toFloat / getHeight.toFloat > input.getWidth.toFloat / input.getHeight.toFloat)
      getWidth.toFloat / input.getWidth.toFloat
    else
      getHeight.toFloat / input.getHeight.toFloat

    val x: Float = (this.getWidth.toFloat - input.getWidth.toFloat * scale) / 2
    val y: Float = (this.getHeight.toFloat - input.getHeight.toFloat * scale) / 2

    canvas.drawBitmap(input,
              new Rect(0, 0, input.getWidth, input.getHeight),
              new RectF(x, y, input.getWidth.toFloat * scale, input.getHeight.toFloat * scale), null)

    if (points != null){
      points.foreach(p => {
        drawingPoints += new Position(x + p.x * scale, y + p.y * scale)
      })
    }

    HistoryManager.forcePush
  }

  private var savedBitmap: Bitmap = null;
  private var savedPoints: Array[Position] = null;

  def reset(): Unit = {
    handler.removeCallbacks(repaintTick)
    if (waterPaint != null)
      waterPaint.kill()
    drawingPoints.clear()
    bitmap = null;
    if (initialized)
      init()
    invalidate()
  }

  def setTool(id: Int): Unit = {
    val ntool = id match {
      case R.id.set_pen => Pen
      case R.id.set_feltpen => FeltPen
      case R.id.set_eraser => Eraser
      case R.id.set_water_paint => PaintBukkit
      case R.id.set_scribble => Scribble
      case R.id.set_spray => Spray
      case R.id.set_wave => Wave
      case R.id.set_chain => Chain
      case R.id.set_grid => Grid
    }

    tool = ntool

    tool.setColor(activeColor)
  }

  def setColor(color: Int): Unit = {
    activeColor = color
    tool.setColor(color)
  }

  def load(state: CanvasState) = {
    bitmap = state.drawing.copy(bitmap.getConfig, true)
    canvas.setBitmap(bitmap)
    drawingPoints = state.drawingPoints.clone()

    invalidate()
  }


  override def onTouchEvent(ev: MotionEvent): Boolean = {
    MainActivity.hideGui()

    val p = Position(ev.getX, ev.getY)
    ev.getAction match {
      case MotionEvent.ACTION_DOWN => {
        tool.start(p)
        lastPoint = p
      }
      case MotionEvent.ACTION_MOVE => {
        if (lastPoint == null)
          return true
        tool.move(p)
        lastPoint = p
      }
      case _ => {
        if (lastPoint == null)
          return true
        tool.move(p)
        tool.end(p)
        lastPoint = null
      }
    }

    invalidate()

    if (ev.getAction == MotionEvent.ACTION_UP && tool != PaintBukkit) {
      HistoryManager.onDrawEnd
    }else if (ev.getAction != MotionEvent.ACTION_DOWN){
      HistoryManager.onDrawStart
    }

    return true
  }

  def inner(p: Position) = p.y >= 0 && p.y < getHeight && p.x >= 0 && p.x < getWidth

  protected override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawBitmap(bitmap, 0, 0, null)

    //ideiglenes paca kifestese
    if (waterPaint.hasBitmap)
      canvas.drawBitmap(waterPaint.resized, 0, 0, AntCanvas.bgPaint);
  }
}


object AntCanvas{
  val bgPaint: Paint = new Paint()
  bgPaint.setColor(Color.WHITE)
  bgPaint.setAntiAlias(true);
  bgPaint.setFilterBitmap(true);
  bgPaint.setDither(true);

}