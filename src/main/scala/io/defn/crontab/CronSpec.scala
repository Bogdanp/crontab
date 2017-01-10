package io.defn.crontab

final case class Minute(val value: Int) extends AnyVal
final case class Hour(val value: Int) extends AnyVal
final case class Day(val value: Int) extends AnyVal

/*** Sum type representing the months of the year. */
sealed trait Month extends Ordering[Month] {
  def compare(a: Month, b: Month): Int =
    a.ord compare b.ord

  def ord: Int =
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

/*** Sum type representing the days of the week. */
sealed trait Weekday extends Ordering[Weekday] {
  def compare(a: Weekday, b: Weekday): Int =
    a.ord compare b.ord

  def ord: Int =
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

final case object Mon extends Weekday
final case object Tue extends Weekday
final case object Wed extends Weekday
final case object Thu extends Weekday
final case object Fri extends Weekday
final case object Sat extends Weekday
final case object Sun extends Weekday

sealed trait Field[+T]
final case class Every[T]() extends Field[T]
final case class Exact[T](value: T) extends Field[T]
final case class Range[T](from: T, to: T) extends Field[T]
final case class Step[T](range: Field[T], step: Int) extends Field[T]
final case class Sequence[T](fields: Seq[Field[T]]) extends Field[T]

case class CronSpec(
  minute: Field[Minute],
  hour: Field[Hour],
  day: Field[Day],
  month: Field[Month],
  weekday: Field[Weekday]
)
