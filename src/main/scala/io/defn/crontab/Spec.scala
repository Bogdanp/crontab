package io.defn.crontab

import java.time.ZonedDateTime

import org.parboiled2.ParseError

import scala.util.{Failure, Success}


trait CanMatch extends Ordered[CanMatch] {
  def value: Int

  def compare(that: CanMatch): Int =
    value - that.value

  def matches(other: Int): Boolean =
    value == other

  def plus(n: Int): CanMatch
}


final case class Minute(val value: Int) extends CanMatch {
  require(0 <= value, "minutes must be >=0")
  require(value < 60, "minutes must be <60")

  def plus(n: Int): Minute =
    Minute((value + n) % 60)
}


final case class Hour(val value: Int) extends CanMatch {
  require(0 <= value, "hours must be >=0")
  require(value < 24, "hours must be <24")

  def plus(n: Int): Hour =
    Hour((value + n) % 24)
}


final case class Day(val value: Int) extends CanMatch {
  require(1 <= value, "days must be >=1")
  require(value < 32, "days must be <32")

  def plus(n: Int): Day =
    Day((value + n - 1) % 31 + 1)
}


/** Sum type representing the months of the year. */
sealed trait Month extends CanMatch {
  import Month._

  def plus(n: Int): Month =
    Month((value + n - 1) % 12 + 1)

  lazy val value: Int =
    this match {
      case Jan => 1
      case Feb => 2
      case Mar => 3
      case Apr => 4
      case May => 5
      case Jun => 6
      case Jul => 7
      case Aug => 8
      case Sep => 9
      case Oct => 10
      case Nov => 11
      case Dec => 12
    }

  lazy val daysInMonth: Seq[Int] =
    this match {
      case Jan => Seq(31)
      case Feb => Seq(28, 29)
      case Mar => Seq(31)
      case Apr => Seq(30)
      case May => Seq(31)
      case Jun => Seq(30)
      case Jul => Seq(31)
      case Aug => Seq(31)
      case Sep => Seq(30)
      case Oct => Seq(31)
      case Nov => Seq(30)
      case Dec => Seq(31)
    }
}

object Month {
  def apply(month: Int): Month =
    month match {
      case 1 => Jan
      case 2 => Feb
      case 3 => Mar
      case 4 => Apr
      case 5 => May
      case 6 => Jun
      case 7 => Jul
      case 8 => Aug
      case 9 => Sep
      case 10 => Oct
      case 11 => Nov
      case 12 => Dec
      case _  => throw new RuntimeException(s"invalid month '${month}'")
    }

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
sealed trait Weekday extends CanMatch {
  import Weekday._

  def plus(n: Int): Weekday =
    Weekday((value + n - 1) % 7 + 1)

  lazy val value: Int =
    this match {
      case Mon => 1
      case Tue => 2
      case Wed => 3
      case Thu => 4
      case Fri => 5
      case Sat => 6
      case Sun => 7
    }
}

object Weekday {
  def apply(weekday: Int): Weekday =
    weekday match {
      case 1 => Mon
      case 2 => Tue
      case 3 => Wed
      case 4 => Thu
      case 5 => Fri
      case 6 => Sat
      case 7 => Sun
      case 0 => Sun
      case _  => throw new RuntimeException(s"invalid weekday '${weekday}'")
    }

  final case object Mon extends Weekday
  final case object Tue extends Weekday
  final case object Wed extends Weekday
  final case object Thu extends Weekday
  final case object Fri extends Weekday
  final case object Sat extends Weekday
  final case object Sun extends Weekday
}


sealed trait Field[+T <: CanMatch] {
  def matches(value: Int): Boolean =
    this match {
      case Field.Every() =>
        true

      case Field.Exact(f) =>
        f.matches(value)

      case Field.Range(a, b, step) => {
        def range(x: CanMatch, s: Int = 0): Stream[CanMatch] =
          (s, x == b) match {
            case (0, true)  => Stream(x)
            case (_, true)  => Stream.empty
            case (0, false) => x #:: range(x.plus(1), step - 1)
            case (_, false) => range(x.plus(1), s - 1)
          }

        range(a).exists(_.matches(value))
      }

      case Field.Sequence(fs) =>
        fs.exists(_.matches(value))
    }
}

object Field {
  final case class Every[T <: CanMatch]() extends Field[T]
  final case class Exact[T <: CanMatch](value: T) extends Field[T]
  final case class Range[T <: CanMatch](from: T, to: T, step: Int = 1) extends Field[T]
  final case class Sequence[T <: CanMatch](fields: Seq[Field[T]]) extends Field[T]
}


/** A structure representing a crontab entry's time and date
  * specification.
  */
case class Spec(
  minute: Field[Minute],
  hour: Field[Hour],
  day: Field[Day],
  month: Field[Month],
  weekday: Field[Weekday]) {

  /** Returns a [[Stream]] of [[ZonedDateTime]]s that match this spec
    * starting from the very next minute. */
  def dateTimes: Stream[ZonedDateTime] =
    dateTimes(ZonedDateTime.now().plusMinutes(1))

  /** Returns a [[Stream]] of [[ZonedDateTime]]s that match this spec
    * starting from [[start]]. */
  def dateTimes(start: ZonedDateTime): Stream[ZonedDateTime] = {
    val dateTime = next(start)

    dateTime #:: dateTimes(dateTime.plusMinutes(1))
  }

  /** Returns `true` if this [[Spec]] matches the given
    * [[ZonedDateTime]]. */
  def matches(dateTime: ZonedDateTime): Boolean =
    ( minute.matches(dateTime.getMinute())
      && hour.matches(dateTime.getHour())
      && day.matches(dateTime.getDayOfMonth())
      && month.matches(dateTime.getMonthValue())
      && weekday.matches(dateTime.getDayOfWeek().getValue())
    )

  private[this] def next(start: ZonedDateTime): ZonedDateTime = {
    var now = start.withSecond(0).withNano(0)

    while (!matches(now))
      now = now.plusMinutes(1)

    now
  }
}


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
