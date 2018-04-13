package org.marktomko.geoducks

import scala.annotation.tailrec

package object util {

  def fastSplit(s: String, delim: Char): List[String] = {
    val di = delim.toInt
    @tailrec
    def go(end: Int, elements: List[String]): List[String] = {
      val dpos = s.lastIndexOf(di, end - 1)
      if (dpos < 0) s.substring(0, end) :: elements
      else go(dpos, s.substring(dpos + 1, end) :: elements)
    }
    go(s.length, Nil)
  }

  def fastSplit(s: String, delim: Char, arr: Array[String]): Int = {
    val di = delim.toInt
    @tailrec
    def go(strIdx: Int, arrIdx: Int): Int = {
      if (arrIdx >= arr.length) arrIdx
      else {
        val pos = s.indexOf(di, strIdx)
        if (pos < 0) {
          arr(arrIdx) = s.substring(strIdx)
          arrIdx + 1
        }
        else {
          arr(arrIdx) = s.substring(strIdx, pos)
          go(pos + 1, arrIdx + 1)
        }
      }
    }
    go(0, 0)
  }

}
