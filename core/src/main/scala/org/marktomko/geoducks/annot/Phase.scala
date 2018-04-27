package org.marktomko.geoducks.annot

import scala.annotation.switch

sealed trait Phase {
  def value: Int
}

case object Phase0 extends Phase {
  override val value = 0
}

case object Phase1 extends Phase {
  override val value = 1
}

case object Phase2 extends Phase {
  override val value = 2
}

object Phase {

  def fromInt(i: Int): Option[Phase] = {
    (i: @switch) match {
      case 0 => Some(Phase0)
      case 1 => Some(Phase1)
      case 2 => Some(Phase2)
      case _ => None
    }
  }

}