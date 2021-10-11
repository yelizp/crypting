package com.example.common

import java.util.{Timer, TimerTask}
import scala.concurrent.duration._

class ScheduledTask {
  implicit def function2TimerTask(f: () => Unit): TimerTask = {
    new TimerTask {
      def run() = f()
    }
  }

  def run(f:() => Unit, durationInMinutes:Int): Timer = {
    val timer = new Timer()
    timer.scheduleAtFixedRate(function2TimerTask(f),0, Duration(durationInMinutes, MINUTES).toMillis)
    timer
  }
}