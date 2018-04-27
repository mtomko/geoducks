package org.marktomko.geoducks.bench

import java.nio.file.{Path, Paths}

import cats.effect.{IO, Sync}
import fs2.{Stream, io, text}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.marktomko.geoducks.format
import org.marktomko.geoducks.seq.Fastq

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object GeoducksBench {

  def main(args: Array[String]): Unit = {
    println("IO:")
    args.foreach { arg =>
      val (r, t) = withIO(arg)
      println(s"${r / t} reads/ms")
    }
    println("Monix:")
    args.foreach { arg =>
      val (r, t) = withIO(arg)
      println(s"${r / t} reads/ms")
    }

  }

  @inline
  final def withMonix(arg: String): (Int, Float) = {
    val task = countGff3Features[Task](Paths.get(arg))
    nanoTimed(Await.result(task.runAsync, Duration.Inf))
  }

  @inline
  final def withIO(arg: String): (Int, Float) = {
    val io = countGff3Features[IO](Paths.get(arg))
    nanoTimed(io.unsafeRunSync())
  }

  final def countFastqReads[F[_]: Sync](path: Path): F[Int] =
    fastq[F](path).compile.fold(0) { case (x, _) => x + 1 }

  final def countGff3Features[F[_]: Sync](path: Path): F[Int] =
    io.file
    .readAll(path, 4096)
    .through(text.utf8Decode)
    .through(text.lines)
    .filter(_.nonEmpty)
    .evalMap(format.gff3[F])
    .compile.fold(0) { case (x, _) => x + 1}

  final def fastq[F[_]: Sync](path: Path): Stream[F, Fastq] =
    io.file
      .readAll(path, 8192)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(format.fastq[F])

}
