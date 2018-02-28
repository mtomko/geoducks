package org.marktomko.geoducks.bench.GeoducksBench

import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import cats.effect.{IO, Sync}
import fs2.{io, text}
import org.marktomko.geoducks.format

object GeoducksBench {

  def nanoTimed(f: => Unit): Float = {
    val t0 = System.nanoTime()
    f
    val t1 = System.nanoTime()
    (t1 - t0).nanos.toUnit(TimeUnit.MILLISECONDS).toFloat
  }

  def main(args: Array[String]): Unit = {
    args.foreach { arg =>
     println(nanoTimed(fastqSequences[IO](Paths.get(arg)).unsafeRunSync))
    }
  }

  def fastqSequences[F[_] : Sync](path: Path): F[Unit] = {
    io.file.readAll[F](path, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(format.fastq)
      .map(_.seq)
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(Paths.get(path.toString + ".txt")))
      .compile.drain
  }

}

