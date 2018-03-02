package org.marktomko.geoducks.stream

import fs2.{Pipe, Pull, Stream}

object StreamOps {

  object pipe {
    def grouped[F[_], O](n: Int): Pipe[F, O, List[O]] = {
      require(n > 0, s"$n must be > 0")
      def go(s: Stream[F, O]): Pull[F, List[O], Unit] =
        s.pull.unconsN(n.toLong).flatMap {
          case None           => Pull.done
          case Some((hd, tl)) => Pull.output1(hd.force.toList) >> go(tl)
        }
      in =>
        go(in).stream
    }
  }

  implicit class GroupedStream[F[_], O](val s: Stream[F, O]) {
    def grouped(n: Int): Stream[F, List[O]] = s.through(pipe.grouped(n))
  }

}
