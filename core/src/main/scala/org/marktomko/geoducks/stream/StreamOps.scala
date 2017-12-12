package org.marktomko.geoducks.stream

import fs2.{Pipe, Pull, Stream}

object StreamOps {

  object stream {

    /** The simplest form of `grouped` to use `Vector` */
    def grouped[F[_], O](s: Stream[F, O], n: Int): Stream[F, Vector[O]] = {
      require(n > 0, s"$n must be > 0")
      def go(s: Stream[F, O]): Pull[F, Vector[O], Unit] =
        s.pull.unconsN(n.toLong).flatMap {
          case None           => Pull.done
          case Some((hd, tl)) => Pull.output1(hd.force.toVector) >> go(tl)
        }
      go(s).stream
    }

  }

  def grouped[F[_], O](n: Int): Pipe[F, O, Vector[O]] = {
    require(n > 0, s"$n must be > 0")
    def go(s: Stream[F, O]): Pull[F, Vector[O], Unit] =
      s.pull.unconsN(n.toLong).flatMap {
        case None           => Pull.done
        case Some((hd, tl)) => Pull.output1(hd.force.toVector) >> go(tl)
      }
    in =>
      go(in).stream
  }

  implicit class GroupedStream[F[_], O](val s: Stream[F, O]) {
    def grouped(n: Int): Stream[F, Vector[O]] = StreamOps.stream.grouped(s, n)
  }

}
