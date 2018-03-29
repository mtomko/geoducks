package org.marktomko.geoducks

import java.io.{BufferedReader, FileReader}
import java.nio.file.Path

import cats.effect.Sync
import fs2.Stream

package object io {

  final def bracketedReader[F[_]: Sync](path: Path): Stream[F, BufferedReader] =
    Stream.bracket(Sync[F].delay(new BufferedReader(new FileReader(path.toFile))))(
      rdr => Stream.emit(rdr),
      rdr => Sync[F].delay(rdr.close()))

}
