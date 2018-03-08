package org.marktomko.geoducks.bench.GeoducksBench

import java.io.{BufferedReader, FileReader}
import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration._
import scala.concurrent.Await

import cats.effect.{IO, Sync}
import fs2.{io, text, Stream}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.marktomko.geoducks.domain.Fastq
import org.marktomko.geoducks.format
import org.marktomko.geoducks.stream.pipe


object GeoducksBench {

  final def nanoTimed[A](f: => A): (A, Float) = {
    val t0 = System.nanoTime()
    val ret = f
    val t1 = System.nanoTime()
    val dt = (t1 - t0).nanos.toUnit(TimeUnit.MILLISECONDS).toFloat
    (ret, dt)
  }

  def main(args: Array[String]): Unit = {
    println("IO:")
    args.foreach { arg =>
      val (r, t) = withIO(arg)
      println(s"${ r / t } reads/ms")
    }
    println("Monix:")
    args.foreach { arg =>
      val (r, t) = withIO(arg)
      println(s"${ r / t } reads/ms")
    }

  }

  @inline
  final def withMonix(arg: String): (Int, Float) = {
    val task = countFastqReads2[Task](Paths.get(arg))
    nanoTimed(Await.result(task.runAsync, Duration.Inf))
  }

  @inline
  final def withIO(arg: String): (Int, Float) = {
    val io = countFastqReads2[IO](Paths.get(arg))
    nanoTimed(io.unsafeRunSync())
  }

  final def fastqSequences[F[_] : Sync](path: Path): F[Unit] = {
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

  final def countFastqReads[F[_] : Sync](path: Path): F[Int] =
    io.file.readAll[F](path, 16384)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(pipe.grouped(4))
      .compile.fold(0) { case (x, _) => x + 1 }

  final def countFastqReads2[F[_] : Sync](path: Path): F[Int] =
    fastqStream[F](path).compile.fold(0) { case (x, _) => x + 1 }

  final def fastqStream[F[_] : Sync](path: Path): Stream[F, Fastq] =
    bracketedReader(path).flatMap(format.fastqStream(_))

  final def bracketedReader[F[_] : Sync](path: Path): Stream[F, BufferedReader] = {
    Stream.bracket(Sync[F].delay(new BufferedReader(new FileReader(path.toFile))))(
      rdr => Stream.emit(rdr),
      rdr => Sync[F].delay(rdr.close()))
  }

}
