package io.defn.crontab

case class Minute(val value: Int) extends AnyVal
case class Hour(val value: Int) extends AnyVal
case class Day(val value: Int) extends AnyVal

sealed trait Month
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

sealed trait Weekday
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
