package org.marktomko.geoducks

import fs2.{Pipe, Pull, Stream}

import scala.collection.immutable.Queue

object StreamOps {

  // todo: pull request into Stream
  object stream {

    /** An extremely inefficient form of grouped based on `sliding`, which
      * generates and discards a lot of junk */
    def groupedQueue[F[_], O](s: Stream[F, O], n: Int): Stream[F, Queue[O]] = {
      require(n > 0, "n must be > 0")
      def go(window: Queue[O], s: Stream[F, O]): Pull[F, Queue[O], Unit] =
        s.pull.unconsN(n.toLong).flatMap {
          case None => Pull.done
          case Some((hd, tl)) =>
            hd.scan(window)((w, i) => w.dequeue._2.enqueue(i)).drop(n.toLong) match {
              case Left((w2, _)) => go(w2, tl)
              case Right(out) =>
                Pull.segment(out).flatMap { window =>
                  go(window, tl)
                }
            }
        }
      s.pull
        .unconsN(n.toLong)
        .flatMap {
          case None => Pull.done
          case Some((hd, tl)) =>
            val window = hd.fold(Queue.empty[O])(_.enqueue(_)).run
            Pull.output1(window) *> go(window, tl)
        }
        .stream
    }

    /** The simplest form of `grouped` to use `Vector` */
    def grouped[F[_], O](s: Stream[F, O], n: Int): Stream[F, Vector[O]] = {
      require(n > 0, "n must be > 0")
      def go(s: Stream[F, O]): Pull[F, Vector[O], Unit] =
        s.pull.unconsN(n.toLong).flatMap {
          case None           => Pull.done
          case Some((hd, tl)) => Pull.output1(hd.toVector) *> go(tl)
        }
      go(s).stream
    }

  }

  def grouped[F[_], O](n: Int): Pipe[F, O, Vector[O]] = {
    require(n > 0, "n must be > 0")
    def go(s: Stream[F, O]): Pull[F, Vector[O], Unit] =
      s.pull.unconsN(n.toLong).flatMap {
        case None           => Pull.done
        case Some((hd, tl)) => Pull.output1(hd.toVector) *> go(tl)
      }
    in =>
      go(in).stream
  }

  implicit class GroupedStream[F[_], O](val s: Stream[F, O]) {
    def groupedQueue(n: Int): Stream[F, Queue[O]] = StreamOps.stream.groupedQueue(s, n)
    def grouped(n: Int): Stream[F, Vector[O]]     = StreamOps.stream.grouped(s, n)
  }

}
