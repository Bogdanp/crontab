package io.defn.crontab

import org.parboiled2.ParseError

import scala.util.{Failure, Success}


final case class Minute(val value: Int) extends AnyVal
final case class Hour(val value: Int) extends AnyVal
final case class Day(val value: Int) extends AnyVal


/** Sum type representing the months of the year. */
sealed trait Month extends Ordering[Month] {
  def compare(a: Month, b: Month): Int =
    a.ord compare b.ord

  lazy val ord: Int =
    this match {
      case Month.Jan => 1
      case Month.Feb => 2
      case Month.Mar => 3
      case Month.Apr => 4
      case Month.May => 5
      case Month.Jun => 6
      case Month.Jul => 7
      case Month.Aug => 8
      case Month.Sep => 9
      case Month.Oct => 10
      case Month.Nov => 11
      case Month.Dec => 12
    }
}

object Month {
  final case object Jan extends Month
  final case object Feb extends Month
  final case object Mar extends Month
  final case object Apr extends Month
  final case object May extends Month
  final case object Jun extends Month
  final case object Jul extends Month
  final case object Aug extends Month
  final case object Sep extends Month
  final case object Oct extends Month
  final case object Nov extends Month
  final case object Dec extends Month
}


/** Sum type representing the days of the week. */
sealed trait Weekday extends Ordering[Weekday] {
  def compare(a: Weekday, b: Weekday): Int =
    a.ord compare b.ord

  lazy val ord: Int =
    this match {
      case Weekday.Mon => 1
      case Weekday.Tue => 2
      case Weekday.Wed => 3
      case Weekday.Thu => 4
      case Weekday.Fri => 5
      case Weekday.Sat => 6
      case Weekday.Sun => 7
    }
}

object Weekday {
  final case object Mon extends Weekday
  final case object Tue extends Weekday
  final case object Wed extends Weekday
  final case object Thu extends Weekday
  final case object Fri extends Weekday
  final case object Sat extends Weekday
  final case object Sun extends Weekday
}


sealed trait Field[+T]

object Field {
  final case class Every[T]() extends Field[T]
  final case class Exact[T](value: T) extends Field[T]
  final case class Range[T](from: T, to: T) extends Field[T]
  final case class Step[T](range: Field[T], step: Int) extends Field[T]
  final case class Sequence[T](fields: Seq[Field[T]]) extends Field[T]
}


/** A structure representing a crontab entry's time and date
  * specification.
  */
case class Spec(
  minute: Field[Minute],
  hour: Field[Hour],
  day: Field[Day],
  month: Field[Month],
  weekday: Field[Weekday]
)


/** Parses crontab time and date specs according to `man 5 crontab`.
  *
  * {{{
  * scala> val Right(c) = Spec.parse("* * * * *")
  * c: Spec = Spec(Every(), Every(), Every(), Every(), Every())
  * }}}
  *
  * Parsing can fail:
  *
  * {{{
  * scala> val Left(error) = Spec.parse("foo")
  * error: String =
  * Invalid input 'f', expected a spec like '0 * * * Sun' or a named rule like '@daily' (line 1, column 1):
  * foo
  * ^
  * }}}
  */
object Spec {
  import Field._
  import Month._
  import Weekday._

  /** Attempt to parse a string to a [[Spec]].  Returns either
    * [[Spec]] on success or [[String]] on error
    * indicating the parse error.
    */
  def parse(input: String): Either[String, Spec] = {
    val parser = new SpecParser(input)

    parser.toplevel.run() match {
      case Success(c) =>
        Right(c)

      case Failure(e: ParseError) =>
        Left(parser.formatError(e))

      case Failure(e) =>
        Left(s"Unexpected error while parsing: ${e.getMessage}")
    }
  }

  /** A [[Spec]] representing a job that is run on a yearly basis. */
  lazy val yearly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Exact(Day(1)),
    month   = Exact(Jan),
    weekday = Every[Weekday]
  )

  /** A [[Spec]] representing a job that is run on a monthly basis. */
  lazy val monthly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Exact(Day(1)),
    month   = Every[Month],
    weekday = Every[Weekday]
  )

  /** A [[Spec]] representing a job that is run every Sunday. */
  lazy val weekly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Every[Day],
    month   = Every[Month],
    weekday = Exact(Sun)
  )

  /** A [[Spec]] representing a job that is run on a daily basis. */
  lazy val daily = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Every[Day],
    month   = Every[Month],
    weekday = Every[Weekday]
  )

  /** A [[Spec]] representing a job that is run on an hourly basis. */
  lazy val hourly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Every[Hour],
    day     = Every[Day],
    month   = Every[Month],
    weekday = Every[Weekday]
  )
}
