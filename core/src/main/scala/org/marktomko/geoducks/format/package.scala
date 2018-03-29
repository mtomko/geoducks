package org.marktomko.geoducks

import fs2.{Pipe, Stream}
import java.io.BufferedReader
import org.marktomko.geoducks.domain.Fastq
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

}
