package io.defn.crontab

import java.time.ZonedDateTime

import org.parboiled2.ParseError

import scala.util.{Failure, Success}


trait CanMatch[Self <: CanMatch[_]] extends Ordered[Self] {
  /** This instance's Int representation.  Used for comparisons and matching. */
  def value: Int

  def plus(n: Int): Self

  /** Compares two instances of the same type for value equality. */
  def compare(that: Self): Int =
    value - that.value

  /** Returns `true` if this instance's value representation is the same as `other`. */
  def matches(other: Int): Boolean =
    value == other
}


/** Wraps minute values. */
final case class Minute(val value: Int) extends CanMatch[Minute] {
  require(0 <= value, "minutes must be >=0")
  require(value < 60, "minutes must be <60")

  def plus(n: Int): Minute =
    Minute((value + n) % 60)
}


/** Wraps hour values. */
final case class Hour(val value: Int) extends CanMatch[Hour] {
  require(0 <= value, "hours must be >=0")
  require(value < 24, "hours must be <24")

  def plus(n: Int): Hour =
    Hour((value + n) % 24)
}


/** Wraps day-of-the-month values. */
final case class Day(val value: Int) extends CanMatch[Day] {
  require(1 <= value, "days must be >=1")
  require(value < 32, "days must be <32")

  def plus(n: Int): Day =
    Day((value + n - 1) % 31 + 1)
}


/** Sum type representing the months of the year. */
sealed trait Month extends CanMatch[Month] {
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

  lazy val maxDaysInMonth: Int =
    this match {
      case Jan => 31
      case Feb => 29
      case Mar => 31
      case Apr => 30
      case May => 31
      case Jun => 30
      case Jul => 31
      case Aug => 31
      case Sep => 30
      case Oct => 31
      case Nov => 30
      case Dec => 31
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
sealed trait Weekday extends CanMatch[Weekday] {
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


/** Represents the individual fields on a [[Spec]], constrained to
  * specific field types based on the position in said [[Spec]]. */
sealed trait Field[+T <: CanMatch[_]] {
  def matches(value: Int): Boolean =
    this match {
      case Field.Every() =>
        true

      case Field.Exact(f) =>
        f.matches(value)

      case r@Field.Range(_, _, _) =>
        r.exists(_.matches(value))

      case Field.Sequence(fs) =>
        fs.exists(_.matches(value))
    }
}

object Field {
  final case class Every[T <: CanMatch[T]]() extends Field[T]
  final case class Exact[T <: CanMatch[T]](value: T) extends Field[T]
  final case class Range[T <: CanMatch[T]](from: T, to: T, step: Int = 1) extends Field[T] {
    require(from < to, s"${from} must be strictly less than ${to}")

    private[this] def range: Stream[T] =
      range(from)

    private[this] def range(x: T, s: Int = 0): Stream[T] =
      (s, x == to) match {
        case (0, true)  => Stream(x)
        case (_, true)  => Stream.empty
        case (0, false) => x #:: range(x.plus(1), step - 1)
        case (_, false) => range(x.plus(1), s - 1)
      }

    def exists(p: T => Boolean): Boolean =
      range.exists(p)
  }

  final case class Sequence[T <: CanMatch[T]](fields: Seq[Field[T]]) extends Field[T]
}


/** Raised at runtime when constructing invalid [[Spec]] instances. */
case class InvalidSpec(message: String = "") extends Exception(message)


/** Represents a crontab entry's time and date specification. */
case class Spec(
  minute: Field[Minute],
  hour: Field[Hour],
  day: Field[Day],
  month: Field[Month],
  weekday: Field[Weekday]) {

  import Field._

  validate()

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

  /** Returns `true` if this [[Spec]] matches the given `dateTime`. */
  def matches(dateTime: ZonedDateTime): Boolean =
    ( minute.matches(dateTime.getMinute())
      && hour.matches(dateTime.getHour())
      && day.matches(dateTime.getDayOfMonth())
      && month.matches(dateTime.getMonthValue())
      && weekday.matches(dateTime.getDayOfWeek().getValue())
    )

  /** Returns the next [[ZonedDateTime]] after [[start]]. */
  private[this] def next(start: ZonedDateTime): ZonedDateTime = {
    var now = start.withSecond(0).withNano(0)

    while (!matches(now))
      now = now.plusMinutes(1)

    now
  }

  /** Ensures that *some* impossible Specs cannot be represented. */
  private[this] def validate(): Unit = {
    (day, month) match {
      case (Exact(Day(d)), Exact(m)) =>
        if (d > m.maxDaysInMonth)
          throw new InvalidSpec(s"day cannot be $d for month $m")

      case (r@Range(_, _, _), Exact(m)) =>
        if (!r.exists(_.value <= m.maxDaysInMonth))
          throw new InvalidSpec(s"range of days cannot be $r for month $m")

      case _ =>
        ()
    }
  }
}


/** Parses crontab time and date specs according to `man 5 crontab`.
  *
  * See [[crontab]] for more information.
  */
object Spec {
  import Field._
  import Month._
  import Weekday._

  /** Attempt to parse a string to a [[Spec]].  Returns either
    * a [[Spec]] or a string indicating the parse error.
    */
  def parse(input: String): Either[String, Spec] = {
    val parser = new SpecParser(input)

    parser.toplevel.run() match {
      case Success(c) =>
        Right(c)

      case Failure(e: ParseError) =>
        Left(parser.formatError(e))

      case Failure(e: InvalidSpec) =>
        Left(s"Invalid spec: ${e.getMessage}")

      case Failure(e) =>
        Left(s"Unexpected error while parsing: ${e.getMessage}")
    }
  }

  /** Represents a job that is run on an annual basis. */
  lazy val yearly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Exact(Day(1)),
    month   = Exact[Month](Jan),
    weekday = Every[Weekday]
  )

  /** Alias for [[yearly]]. */
  lazy val annually = yearly

  /** Represents a job that is run on a monthly basis. */
  lazy val monthly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Exact(Day(1)),
    month   = Every[Month],
    weekday = Every[Weekday]
  )

  /** Represents a job that is run every Sunday. */
  lazy val weekly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Every[Day],
    month   = Every[Month],
    weekday = Exact[Weekday](Sun)
  )

  /** Represents a job that is run on a daily basis. */
  lazy val daily = Spec(
    minute  = Exact(Minute(0)),
    hour    = Exact(Hour(0)),
    day     = Every[Day],
    month   = Every[Month],
    weekday = Every[Weekday]
  )

  /** Alias for [[daily]]. */
  lazy val midnight = daily

  /** Represents a job that is run on an hourly basis. */
  lazy val hourly = Spec(
    minute  = Exact(Minute(0)),
    hour    = Every[Hour],
    day     = Every[Day],
    month   = Every[Month],
    weekday = Every[Weekday]
  )
}
