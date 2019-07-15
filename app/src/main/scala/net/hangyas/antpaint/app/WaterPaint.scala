package net.hangyas.antpaint.app

import android.graphics._
import android.util.Log

import scala.collection.mutable
import scala.util.Random

/**
 * Created by hangyas on 2015-06-08
 */
class WaterPaint(parentWidth: Int, parentHeight: Int, antCanvas: AntCanvas) extends Runnable{

  val scale = 2
  val width = parentWidth / scale
  val height = parentHeight / scale

  //helper things
  val dropPaint = new Paint();
  dropPaint.setStyle(Paint.Style.FILL_AND_STROKE);
  dropPaint.setColor(0xff00ffff)
  val dirs = Array(PositionI(1, 0), PositionI(0, 1), PositionI(-1, 0), PositionI(0, -1))
  val rand = new Random()

  /**
   * erre festjuk az ideiglenes kepet
   * */
  var bitmap: Bitmap = null
  var canvas: Canvas = new Canvas()

  /**
   * szabályozza hogy mi történik
   * true: a paca végtelenig növekszik
   * false: a paca gyorsan elfogy (szépen lezárva), ha vége kikerül a handlerből
   * */
  private var pour: Boolean = false

  /**
   * elindítja a pacacsinálást a pos-ban
   *
   * hozzadja a waterPaintet az antCanvas.handler-hez
   * */
  def start(realpos: Position, color: Int): Unit = {
    val pos = realpos / scale
    activeW = new WeirdHeap[PositionI]()
    activeW.push(new PositionI(pos))
//    active = new PositionI(pos) :: Nil

    var wcolor = (color & 0x00ffffff) | 0xb9000000
    dropPaint.setColor(wcolor)

    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    canvas.setBitmap(bitmap)
    canvas.drawPoint(pos.x, pos.y, dropPaint)

    pour = true
    antCanvas.handler.post(this)
  }

  def stop(): Unit = {
    pour = false
  }

  /**
   * lezaras nelkul leallitja (pl reset-nel amikor ugyse fog kelleni)
   * */
  def kill(): Unit = {
    stop()
    antCanvas.handler.removeCallbacks(this)
  }

  def hasBitmap = bitmap != null

  /**
   * ezzel szeryunk be egy bitmapet amit ki lehet rajzolni
   * */
  def resized = Bitmap.createScaledBitmap(bitmap, parentWidth, parentHeight, false);

  override def run(): Unit = {
    if (pour) {
      flow()
      antCanvas.handler.postDelayed(this, 0);
    }else{
      activeW = null
      antCanvas.handler.removeCallbacks(this)

      antCanvas.canvas.drawBitmap(resized, 0, 0, AntCanvas.bgPaint);
      HistoryManager.forcePush
      bitmap = null // hatha jol jon a ram
    }
  }

//  var active = List[PositionI]();
  var activeW = new WeirdHeap[PositionI]()

  private def flow(): Unit = {
    if (activeW == null || activeW.isEmpty)
      return;

    import scala.collection.JavaConversions._

    val nactives = new WeirdHeap[PositionI]()
    val a = activeW.getPreFix.toSeq

    var i = 0
    var end = a.length
    if(end > 20) {
      end = rand.nextInt(a.length - 10) + 10
      var start = rand.nextInt(end / 2 - 1)
      while (i < start) {
        nactives.push(a(i))
        i += 1
      }
    }

    while (i < end){
      val ap = a(i)
      val oldColor = bitmap.getPixel(ap.x, ap.y)

      dirs.foreach(d => {
        var p = ap + d

        if (isInner(p) && bitmap.getPixel(p.x, p.y) == 0){

          dropPaint.setColor(genColor(oldColor))
          bitmap.setPixel(p.x, p.y, dropPaint.getColor);

          if (Color.alpha(dropPaint.getColor) > 10)
            nactives push p
        }
      })

      i += 1
    }

    while (i < a.length){
      nactives.push(a(i))
      i += 1
    }

    activeW = nactives

  }

  private def genColor(color: Int): Int = {
    val hsv = new Array[Float](3)
    Color.colorToHSV(color, hsv)
    // hsv[0] is Hue [0 .. 360) hsv[1] is Saturation [0...1] hsv[2] is Value [0...1]
    if (rand.nextFloat() < 0.2)
      hsv(0) = (hsv(0) + rand.nextFloat() * 12 - 6) % 360f

    //    hsv(1) = hsv(1) + rand.nextFloat() * 0.05f - 0.025f
    //(((color >> 24) & 0x000000ff)
    var alpha: Int = (Color.alpha(color) + rand.nextFloat() * 8 - 5f).toInt
    if (alpha > 255)
      alpha = 255
    if (alpha < 0)
      alpha = 0


    return Color.HSVToColor(alpha, hsv)
  }

  private def isInner(p: PositionI): Boolean = p.x >= 0 && p.y >= 0 && p.x < width && p.y < height;


}
