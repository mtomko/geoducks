package org.marktomko.geoducks

import com.univocity.parsers.csv.CsvParser
import fs2.{Pipe, Stream}
import java.io.BufferedReader
import org.marktomko.geoducks.domain.Fastq
import org.marktomko.geoducks.stream.pipe.grouped

package object format {

  final def fastq[F[_]]: Pipe[F, String, Fastq] = { in =>
    grouped(4)(in).map {
      case id :: seq :: _ :: qual :: Nil => Fastq(id, seq, qual)
      case _ => throw new AssertionError("bug")
    }
  }

  final def fastqStream(reader: BufferedReader) = {
    Stream.unfold(reader) { r =>
      val line = reader.readLine()
      if (line == null) None
      else {
        val seq = reader.readLine()
        val _ = reader.readLine()
        Some((Fastq(line, seq, reader.readLine()), reader))
      }
    }
  }

  final def toStream[Record](parser: CsvParser) = 
    Stream.unfold(parser) { p =>
      Option(p.parseNextRecord()).map((_, p)) }

}
