package org.marktomko.geoducks.format

import java.io.{BufferedReader, StringReader}

import org.marktomko.geoducks.domain.Fastq
import org.scalatest.{FlatSpec, Matchers}

class FormatTest extends FlatSpec with Matchers {

  val fq =
    """@cluster_2:UMI_ATTCCG
      |TTTCCGGGGCACATAATCTTCAGCCGGGCGC
      |+
      |9C;=;=<9@4868>9:67AA<9>65<=>591
      |@cluster_8:UMI_CTTTGA
      |TATCCTTGCAATACTCTCCGAACGGGAGAGC
      |+
      |1/04.72,(003,-2-22+00-12./.-.4-""".stripMargin

  "fastq" should "read two fastq records" in {
    val br = new BufferedReader(new StringReader(fq))
    try {
      val recs = fastq(br).toList
      recs should have size 2
      recs should be(
        List(
          Fastq(
            "@cluster_2:UMI_ATTCCG",
            "TTTCCGGGGCACATAATCTTCAGCCGGGCGC",
            "9C;=;=<9@4868>9:67AA<9>65<=>591"
          ),
          Fastq(
            "@cluster_8:UMI_CTTTGA",
            "TATCCTTGCAATACTCTCCGAACGGGAGAGC",
            "1/04.72,(003,-2-22+00-12./.-.4-"
          )
        )
      )
      recs
    } finally {
      br.close
    }
  }
  it should "not return results for an incomplete stream" in {
    val incompleteFq = fq.split('\n').dropRight(1).mkString("\n")
    val br = new BufferedReader(new StringReader(incompleteFq))
    try {
      val recs = fastq(br).toList
      recs should have size 1
      recs should be(
        List(
          Fastq(
            "@cluster_2:UMI_ATTCCG",
            "TTTCCGGGGCACATAATCTTCAGCCGGGCGC",
            "9C;=;=<9@4868>9:67AA<9>65<=>591"
          )
        )
      )
      recs
    } finally {
      br.close
    }
  }

}
