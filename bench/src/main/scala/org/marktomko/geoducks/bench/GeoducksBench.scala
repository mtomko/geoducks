package org.marktomko.geoducks.bench

import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Sync}
import fs2.{Stream, io, text}
import org.marktomko.geoducks.format
import org.marktomko.geoducks.seq.Fastq

import scala.concurrent.ExecutionContext

object GeoducksBench {

  def main(args: Array[String]): Unit = {
    val blockingExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

    try {
      println("IO:")
      args.foreach { arg =>
        val (r, t) = withIO(arg, blockingExecutionContext)
        println(s"${r / t} records/ms")
      }
    } finally {
      blockingExecutionContext.shutdown()
    }
  }

  @inline
  final def withIO(arg: String, blockingExecutionContext: ExecutionContext): (Int, Float) = {
    implicit val cs: ContextShift[IO] = cats.effect.IO.contextShift(blockingExecutionContext)
    val io = countFastqReads[IO](Paths.get(arg), blockingExecutionContext)
    nanoTimed(io.unsafeRunSync())
  }

  final def countFastqReads[F[_]: Sync: ContextShift](path: Path, blockingExecutionContext: ExecutionContext): F[Int] =
    fastq[F](path, blockingExecutionContext).compile.fold(0) { case (x, _) => x + 1 }

  final def countGff3Features[F[_]: Sync: ContextShift](
      path: Path,
      blockingExecutionContext: ExecutionContext): F[Int] =
    io.file
      .readAll(path, blockingExecutionContext, 8192)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .evalMap(format.gff3[F])
      .compile
      .fold(0) { case (x, _) => x + 1 }

  final def fastq[F[_]: Sync: ContextShift](path: Path, blockingExecutionContext: ExecutionContext): Stream[F, Fastq] =
    io.file
      .readAll(path, blockingExecutionContext, 8192)
      .through(text.utf8Decode)
      .through(text.lines)
      .through(format.fastq[F])

}
