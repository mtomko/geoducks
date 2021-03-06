package org.marktomko.geoducks.annot

import cats.implicits._
import org.marktomko.geoducks.util.fastSplit

import scala.collection.mutable

sealed trait Gff3Syntax

sealed trait Gff3Pragma extends Gff3Syntax

case class Gff3Feature(
    seqId: String,
    source: Option[String],
    featureType: String,
    start: Int,
    end: Int,
    score: Option[Float],
    strand: Either[Unit, Option[Strand]],
    phase: Option[Phase],
    attributes: Map[String, Seq[String]])
  extends Gff3Syntax

case object ReferencesResolved extends Gff3Pragma

case class GenericPragma(text: String) extends Gff3Pragma

case class Comment(msg: String) extends Gff3Pragma

object Gff3Feature {

  def apply(arr: Array[String]): Gff3Feature =
    Gff3Feature(
      arr(0),
      opt(arr(1)),
      arr(2),
      arr(3).toInt,
      arr(4).toInt,
      opt(arr(5)).map(_.toFloat),
      strand(arr(6)),
      opt(arr(7)).flatMap(s => Phase.fromInt(s.toInt)),
      opt(arr(8)).map(attributes).getOrElse(Map())
    )

  @inline private[this] def opt(s: String): Option[String] =
    if (s == ".") None
    else Some(s)

  @inline private[this] def strand(s: String): Either[Unit, Option[Strand]] =
    s match {
      case "?" => Right(None)
      case "." => Left(())
      case _   => Right(Strand.fromChar(s.head))
    }

  @inline private[this] def attributes(s: String): Map[String, Seq[String]] = {
    val pairs = fastSplit(s, ';')
    val m     = mutable.HashMap[String, mutable.Builder[String, List[String]]]()
    pairs.foreach {
      fastSplit(_, '=') match {
        case k :: v :: Nil => m.getOrElseUpdate(k, List.newBuilder[String]) += v
        case _             => // do nothing, or throw
      }
    }
    m.map { case (k, v) => k -> v.result }.toMap
  }

}

object Gff3Syntax {

  def fromString(s: String): Either[Exception, Gff3Syntax] = {
    val e =
      if (s == "###") Right(ReferencesResolved)
      else if (s.startsWith("##")) Right(GenericPragma(s.substring(2).trim))
      else if (s.startsWith("#")) Right(Comment(s.substring(1).trim))
      else Either.catchNonFatal(Gff3Feature(s.split('\t')))

    e.leftMap(t => new Exception(s"Unable to parse `$s`", t))
  }

}
