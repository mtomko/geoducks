package org.marktomko.geoducks

import fs2.Pipe
import org.marktomko.geoducks.domain.Fastq
import org.marktomko.geoducks.stream.StreamOps.pipe.grouped

package object format {

  def fastq[F[_]]: Pipe[F, String, Fastq] = { in =>
    grouped(4)(in).map { v =>
      Fastq(v(0), v(1), v(3))
    }
  }

}
