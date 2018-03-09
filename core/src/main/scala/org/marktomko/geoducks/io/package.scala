package org.marktomko.geoducks

import com.univocity.parsers.csv.{ CsvParser, CsvParserSettings }
import java.io.{BufferedReader, FileReader}
import java.nio.file.Path

import cats.effect.Sync
import fs2.Stream

package object io {

  final def bracketedReader[F[_] : Sync](path: Path): Stream[F, BufferedReader] = {
    Stream.bracket(Sync[F].delay(new BufferedReader(new FileReader(path.toFile))))(
      rdr => Stream.emit(rdr),
      rdr => Sync[F].delay(rdr.close()))
  }

  final def bracketedCsvParser[F[_] : Sync](settings: CsvParserSettings, path: Path): Stream[F, CsvParser] = {
    val acquire = Sync[F].delay {
      val parser = new CsvParser(settings)
      parser.beginParsing(new FileReader(path.toFile))
      parser
    }
    val close = (p: CsvParser) => Sync[F].delay(p.stopParsing())
    Stream.bracket(acquire)(Stream.emit(_), close)
  }

}
