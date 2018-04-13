package org.marktomko.geoducks

import java.io.BufferedReader
import java.nio.file.Path

import cats.ApplicativeError
import cats.effect.Sync
import fs2.{Pipe, Pull, Stream, text}
import org.marktomko.geoducks.annot.{Gff3Feature, Gff3Syntax}
import org.marktomko.geoducks.seq.{Fasta, Fastq}
import org.marktomko.geoducks.util.fastSplit

package object format {

  /** Converts a [[Stream]] of Strings (assumed to be lines from a file) into a Stream of [[Fastq]]
    * records. This method uses groupd and is much slower than the variant below.
    */
  final def fastq[F[_]]: Pipe[F, String, Fastq] = { in =>
    in.segmentN(4, false).map { seg =>
      seg.force.toList match {
        case id :: seq :: _ :: qual :: Nil => Fastq(id, seq, qual)
        case _                             => throw new AssertionError("bug")
      }
    }
  }

  /** Converts a [[Stream]] of Strings into a Stream of [[Fasta]] records. */
  final def fasta[F[_]]: Pipe[F, String, Fasta] = { in =>
    import scala.collection.mutable

    @inline def idOf(s: String): String = s.substring(1).trim

    // handles reading sequence data and then either finishes up or returns to deal with the next record
    def seq(s: Stream[F, String], id: String, buf: mutable.StringBuilder): Pull[F, Fasta, Unit] =
      s.pull.uncons1.flatMap {
        case None =>
          Pull.output1(Fasta(id, buf.toString().trim)) >> Pull.done
        case Some((hd, tl)) if hd.startsWith(">") =>
          Pull.output1(Fasta(id, buf.toString().trim)) >> seq(tl, idOf(hd), new mutable.StringBuilder())
        case Some((hd, tl)) =>
          seq(tl, id, buf ++= hd)
      }

    in.pull.uncons1.flatMap {
      case Some((hd, tl)) if hd.startsWith(">") =>
        seq(tl, idOf(hd), new mutable.StringBuilder())
      case _ =>
        Pull.done
    }.stream
  }

  def readerToStream[F[_]: Sync](reader: BufferedReader): Stream[F, String] = {
    Stream.unfoldEval(reader) { r =>
      val fl = Sync[F].delay(r.readLine())
      Sync[F].map(fl) { line =>
        Option(line).map(l => (l, r))
      }
    }
  }

  final def gff3[F[_]: Sync](reader: BufferedReader): Stream[F, Gff3Syntax] = {
    val arr = Array.ofDim[String](9)
    readerToStream(reader: BufferedReader).flatMap { line =>
      val fields = fastSplit(line, '\t', arr)
      if (fields != arr.length) Stream.raiseError(new Exception("doh"))
      else Stream(Gff3Feature(arr))
    }
  }

  def gff3[F[_]](s: String)(implicit ae: ApplicativeError[F, Throwable]): F[Gff3Syntax] =
    ae.fromEither(Gff3Syntax.fromString(s))

  def gff3[F[_] : Sync](p: Path): Stream[F, Gff3Syntax] = {
    fs2.io.file
      .readAll[F](p, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .evalMap(gff3[F])
  }

}
