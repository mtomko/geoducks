package org.marktomko.geoducks.util

import org.scalatest.{FlatSpec, Matchers}

class UtilTest extends FlatSpec with Matchers {

  "fastSplit" should "split a string into an array" in {
    val s = "1,2,33,444"
    val a = Array.ofDim[String](4)
    fastSplit(s, ',', a) should be (4)
    a should be (Array("1", "2", "33", "444"))
  }

  "fastSplit" should "split a string into a list" in {
    //       0123456789
    val s = "1,2,33,444"
    fastSplit(s, ',') should be (List("1", "2", "33", "444"))
  }

}
