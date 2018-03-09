package org.marktomko.geoducks

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

package object bench {

  final def nanoTimed[A](f: => A): (A, Float) = {
    val t0 = System.nanoTime()
    val ret = f
    val t1 = System.nanoTime()
    val dt = (t1 - t0).nanos.toUnit(TimeUnit.MILLISECONDS).toFloat
    (ret, dt)
  }

}
