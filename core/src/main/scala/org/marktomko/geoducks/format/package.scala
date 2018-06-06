package org.marktomko.geoducks

import java.nio.file.Path

import cats.implicits._
import cats.ApplicativeError
import cats.effect.Sync
import fs2.{Pipe, Pull, Stream, text}
import org.marktomko.geoducks.annot.Gff3Syntax
import org.marktomko.geoducks.seq.{Fasta, Fastq}

package object format {

  /** Converts a [[Stream]] of Strings (assumed to be lines from a file) into a Stream of [[Fastq]]
    * records.
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

  type SFasta[F[_]] = (String, fs2.Stream[F, Char])

  final def sfasta[F[_]]: Pipe[F, Char, SFasta[F]] = { in =>

    def seq(s: Stream[F, Char], seqId: String): Pull[F, SFasta[F], Unit] = {
      // the dual `takeWhile` / `dropThrough` may be necessary to maintain the laziness of the stream
      Pull.output1((seqId, s.takeWhile(_ =!= '>').filter(_ =!= '\n'))) >> id(s.dropThrough(_ =!= '>'), "")
    }

    def id(s: Stream[F, Char], buf: String): Pull[F, SFasta[F], Unit] = {
      s.pull.uncons1.flatMap {
        case None =>
          Pull.done
        case Some((hd, tl)) if hd === '\n' =>
          seq(tl, buf.trim())
        case Some((hd, tl)) =>
          id(tl, buf + hd)
      }
    }

    in.pull.uncons1.flatMap {
      case Some((hd, tl)) if hd === '>' => id(tl, "")
      case _ => Pull.done
    }.stream
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
