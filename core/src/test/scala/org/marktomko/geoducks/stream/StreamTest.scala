package org.marktomko.geoducks.stream

import fs2.Stream
import org.scalatest.{FlatSpec, Matchers}

class StreamTest extends FlatSpec with Matchers {

  "grouped" should "group a stream" in {
    val s = Stream.range(1, 9)
    s.through(grouped(4)).toList should be (List(Vector(1, 2, 3, 4), Vector(5, 6, 7, 8)))
  }

}
