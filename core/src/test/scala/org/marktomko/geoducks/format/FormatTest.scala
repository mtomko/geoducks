package org.marktomko.geoducks.format

import org.marktomko.geoducks.seq.{Fasta, Fastq}
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
    val stream = fs2.Stream.emits(fq.split('\n'))
    val recs = stream.through(fastq).toList
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
  }
  it should "not return results for an incomplete stream" in {
    val stream = fs2.Stream.emits(fq.split('\n').dropRight(1))
    val recs = stream.through(fastq).toList
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
  }
  "fasta" should "read a fasta with a single sequence" in {
    val fa = Seq(
      "> chr1",
      "TTTCCGGGGCACATAATCTTCAGCCGGGCGC",
      "TATCCTTGCAATACTCTCCGAACGGGAGAGC"
    )
    val ret = fasta(fs2.Stream(fa: _*)).toList
    ret should be (
      List(
        Fasta(
          "chr1",
          "TTTCCGGGGCACATAATCTTCAGCCGGGCGCTATCCTTGCAATACTCTCCGAACGGGAGAGC"
        )
      )
    )
  }
  it should "read a fasta with two sequences" in {
    val fa = Seq(
      "> chr1",
      "TTTCCGGGGCACATAATCTTCAGCCGGGCGC",
      "> chr2",
      "TATCCTTGCAATACTCTCCGAACGGGAGAGC"
    )
    val ret = fasta(fs2.Stream(fa: _*)).toList
    ret should be (
      List(
        Fasta("chr1", "TTTCCGGGGCACATAATCTTCAGCCGGGCGC"),
        Fasta("chr2", "TATCCTTGCAATACTCTCCGAACGGGAGAGC")
      )
    )
  }

}
