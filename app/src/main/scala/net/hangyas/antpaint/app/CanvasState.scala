package net.hangyas.antpaint.app

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import android.graphics.{BitmapFactory, Bitmap}
import android.util.Log
import scala.collection.mutable


/**
  * HistoryManager hozza létre, magától tömörül
  * ha még nincs kész a tömörítés akkor a simát adja vissza
  * a konstruktor LEMÁSOLJA a paramétereket
  *
  * Created by hangyas on 2015-06-08
  */
class CanvasState(
  _drawing: Bitmap,
  _drawingPoints: mutable.MutableList[Position]) {

//  val drawing = _drawing.copy(_drawing.getConfig, false)
//  val drawingPoints = _drawingPoints.clone()

  var ready = false
  var bytes = new ByteArrayOutputStream()
  var drawingPoints: mutable.MutableList[Position] = null

  var tmpDrawing = _drawing.copy(_drawing.getConfig, false)

  new Thread(new Runnable() {
    def run() {
//      var time = System.currentTimeMillis()
      tmpDrawing.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
      drawingPoints = _drawingPoints.clone
      ready = true
      tmpDrawing = null
//      Log.v("history", "ready " + (System.currentTimeMillis() - time) + " " + bytes.size())
    }
  }).start();

  def drawing : Bitmap = {
    while (!ready){
      return tmpDrawing
    }
    return BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()))
  };

}
