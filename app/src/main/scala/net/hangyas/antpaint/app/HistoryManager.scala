package net.hangyas.antpaint.app

import android.util.Log

import scala.collection.mutable

/**
  * onDrawStart és onDrawEnd el szólunk neki hogy mi van, ha egy ideje már nem rajzol
  * akkor magától ment egyet (nem lesz torlódás)
  * van forcePush
  * a CanvasStatek maguakt tömörítik
  *
  * Created by hangyas on 2016-01-22
  */
object HistoryManager {

  private val antCanvas = MainActivity.self.antCanvas

  private var lastChange : Long = 0;

  new Thread(new Runnable {
    override def run(): Unit = {
      Log.v("history", "pushtime")
      while(true){
        if (lastChange != 0 && System.currentTimeMillis() - lastChange > 200){
          lastChange = 0
          HistoryManager.push
        }
        Thread.sleep(50)
      }
    }
  }).start()


  private var history = mutable.MutableList[CanvasState]()
  private var historyIndex = -1;

  def onDrawStart = {
    lastChange = 0
  }

  def onDrawEnd = {
    lastChange = System.currentTimeMillis()
  }

  def forcePush = {
    push
  }

  private def push = {
    var state: CanvasState = new CanvasState(antCanvas.bitmap, antCanvas.drawingPoints)

    //érvénytelenített előzméyn eltüntetése
    if (historyIndex < history.length) {
      history = history.take(historyIndex + 1)
    }

    history += state
    if (history.length > 10)
      history = history.tail
    historyIndex = history.length - 1
  }


  def undo(): Unit = {
    if (historyIndex == 0)
      return

    if (lastChange != 0)
      forcePush

    historyIndex -= 1
    antCanvas.load(history(historyIndex))
  }

  def canUndo: Boolean = historyIndex > 0

  def redo(): Unit = {
    if (historyIndex == history.length - 1)
      return

    if (historyIndex < history.length){
      historyIndex += 1
      antCanvas.load(history(historyIndex))
    }
  }

  def canRedo: Boolean = historyIndex >= 0 && historyIndex < history.length - 1
}
