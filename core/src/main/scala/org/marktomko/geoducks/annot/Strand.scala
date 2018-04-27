package org.marktomko.geoducks.annot

import scala.annotation.switch

trait Strand {
  def charValue: Char
}

case object PlusStrand extends Strand {
  override val charValue = '+'
}

case object MinusStrand extends Strand {
  override val charValue = '-'
}

object Strand {

  def fromChar(c: Char): Option[Strand] = {
    (c: @switch) match {
      case '-' => Some(MinusStrand)
      case '+' => Some(PlusStrand)
      case _   => None
    }
  }

}