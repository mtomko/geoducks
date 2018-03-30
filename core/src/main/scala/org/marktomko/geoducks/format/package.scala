package org.marktomko.geoducks

import java.io.BufferedReader

import fs2.{Pipe, Pull, Stream}
import org.marktomko.geoducks.domain.{Fasta, Fastq}
import org.marktomko.geoducks.stream.grouped

package object format {

  /** Converts a [[Stream]] of Strings (assumed to be lines from a file) into a Stream of [[Fastq]]
    * records. This method uses groupd and is much slower than the variant below.
    */
  final def fastq[F[_]]: Pipe[F, String, Fastq] = { in =>
    grouped(4)(in).map {
      case id :: seq :: _ :: qual :: Nil => Fastq(id, seq, qual)
      case _                             => throw new AssertionError("bug")
    }
  }

  /** Converts a [[BufferedReader]] into a [[Stream]] of [[Fastq]] records.
    *
    * This method assumes that it will be called within the context of [[Stream#bracket]] and does
    * no resource management.
    */
  final def fastq(reader: BufferedReader) =
    Stream.unfold(reader) { r =>
      val line = reader.readLine()
      if (line == null) None
      else {
        val seq  = reader.readLine()
        val _    = reader.readLine()
        val qual = reader.readLine()
        if (qual == null) None
        else Some((Fastq(line, seq, qual), reader))
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

}
