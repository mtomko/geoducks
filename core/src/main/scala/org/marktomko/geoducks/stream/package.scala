package org.marktomko.geoducks

import fs2.{Pipe, Pull, Stream}

package object stream {

  def grouped[F[_], O](n: Int): Pipe[F, O, List[O]] = { in =>
    require(n > 0, s"$n must be > 0")

    def go(s: Stream[F, O]): Pull[F, List[O], Unit] =
      s.pull.unconsN(n.toLong).flatMap {
        case None => Pull.done
        case Some((hd, tl)) => Pull.output1(hd.force.toList) >> go(tl)
      }

    go(in).stream
  }

}
