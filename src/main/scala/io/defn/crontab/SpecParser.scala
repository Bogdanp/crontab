package io.defn.crontab

import org.parboiled2._


private class SpecParser(val input: ParserInput) extends Parser {
  import Field._
  import Month._
  import Weekday._

  type Meta[T] = () => Rule1[T]

  def number: Rule1[Int] = rule {
    capture(oneOrMore(CharPredicate.Digit)) ~> { s: String =>
      s.toInt
    }
  }

  def number(from: Int, until: Int): Rule1[Int] = rule {
    quiet(number) ~> { n: Int =>
      test(n >= from && n < until).named(s"an integer in range [$from, $until)") ~ push(n)
    }
  }

  def monthDigit = rule {
    ( ch('2') ~ push(Feb)
      | ch('3') ~ push(Mar)
      | ch('4') ~ push(Apr)
      | ch('5') ~ push(May)
      | ch('6') ~ push(Jun)
      | ch('7') ~ push(Jul)
      | ch('8') ~ push(Aug)
      | ch('9') ~ push(Sep)
      | str("10") ~ push(Oct)
      | str("11") ~ push(Nov)
      | str("12") ~ push(Dec)
      | ch('1') ~ push(Jan)
    )
  }

  def monthLiteral = rule {
    ( ignoreCase("jan") ~ push(Jan)
      | ignoreCase("feb") ~ push(Feb)
      | ignoreCase("mar") ~ push(Mar)
      | ignoreCase("apr") ~ push(Apr)
      | ignoreCase("may") ~ push(May)
      | ignoreCase("jun") ~ push(Jun)
      | ignoreCase("jul") ~ push(Jul)
      | ignoreCase("aug") ~ push(Aug)
      | ignoreCase("sep") ~ push(Sep)
      | ignoreCase("oct") ~ push(Oct)
      | ignoreCase("nov") ~ push(Nov)
      | ignoreCase("dec") ~ push(Dec)
    )
  }

  def weekdayDigit = rule {
    ( ch('1') ~ push(Mon)
      | ch('2') ~ push(Tue)
      | ch('3') ~ push(Wed)
      | ch('4') ~ push(Thu)
      | ch('5') ~ push(Fri)
      | ch('6') ~ push(Sat)
      | ch('7') ~ push(Sun)
      | ch('0') ~ push(Sun) // Both 0 and 7 are Sunday
    )
  }

  def weekdayLiteral = rule {
    ( ignoreCase("mon") ~ push(Mon)
      | ignoreCase("tue") ~ push(Tue)
      | ignoreCase("wed") ~ push(Wed)
      | ignoreCase("thu") ~ push(Thu)
      | ignoreCase("fri") ~ push(Fri)
      | ignoreCase("sat") ~ push(Sat)
      | ignoreCase("sun") ~ push(Sun)
    )
  }

  val minute  = () => rule { number(0, 60) ~> Minute }
  val hour    = () => rule { number(0, 24) ~> Hour }
  val day     = () => rule { number(1, 32) ~> Day }
  val month   = () => rule { monthDigit | monthLiteral }
  val weekday = () => rule { weekdayDigit | weekdayLiteral }

  def every[T <: CanMatch]: Rule1[Field[T]] = rule {
    ch('*') ~ push(Every())
  }

  def exact[T <: CanMatch](inner: Meta[T]) = rule {
    inner() ~> (Exact(_))
  }

  def range[T <: CanMatch](inner: Meta[T]) = rule {
    inner() ~ '-' ~!~ inner() ~> (Range(_, _))
  }

  def step[T <: CanMatch](inner: Meta[T], first: T, last: T) = rule {
    (range(inner) | every[T]) ~ '/' ~!~ number ~> { (field: Field[T], step: Int) =>
      field match {
        case Every()        => Range(first, last, step)
        case Range(a, b, _) => Range(a, b, step)

        case _ => throw new RuntimeException("impossible case")
      }
    }
  }

  def component[T <: CanMatch](inner: Meta[T]) = rule {
    range(inner) | exact(inner)
  }

  def sequence[T <: CanMatch](inner: Meta[T]) = rule {
    (component(inner)) ~ ',' ~!~ oneOrMore(component(inner)).separatedBy(',') ~> {
      (head: Field[T], tail: Seq[Field[T]]) =>

      Sequence(head +: tail)
    }
  }

  def field[T <: CanMatch](inner: Meta[T], first: T, last: T) = rule {
    ( step(inner, first, last).named("a step")
      | every[T].named("*")
      | sequence(inner).named("a comma-separated sequence")
      | range(inner).named("a range")
      | exact(inner).named("an exact value")
    ) ~ whitespace
  }

  def spec: Rule1[Spec] = rule {
    ( whitespace
      ~ field(minute, first = Minute(0), last = Minute(59)).named("a minute field")
      ~ field(hour, first = Hour(0), last = Hour(23)).named("an hour field")
      ~ field(day, first = Day(1), last = Day(31)).named("a day field")
      ~ field(month, first = Jan, last = Dec).named("a month field")
      ~ field(weekday, first = Mon, last = Sun).named("a weekday field")
      ~ EOI.named("the end of input")
    ) ~> {
      (minute: Field[Minute], hour: Field[Hour], day: Field[Day], month: Field[Month], weekday: Field[Weekday]) =>

      Spec(minute, hour, day, month, weekday)
    }
  }

  def yearly: Rule1[Spec] = rule {
    (ignoreCase("yearly") | ignoreCase("annually")) ~ push(Spec.yearly)
  }

  def monthly: Rule1[Spec] = rule {
    ignoreCase("monthly") ~ push(Spec.monthly)
  }

  def weekly: Rule1[Spec] = rule {
    ignoreCase("weekly") ~ push(Spec.weekly)
  }

  def daily: Rule1[Spec] = rule {
    (ignoreCase("daily") | ignoreCase("midnight")) ~ push(Spec.daily)
  }

  def hourly: Rule1[Spec] = rule {
    ignoreCase("hourly") ~ push(Spec.hourly)
  }

  def special: Rule1[Spec] = rule {
    '@' ~!~ (yearly | monthly | weekly | daily | hourly) ~ EOI.named("the end of input")
  }

  def toplevel: Rule1[Spec] = rule {
    spec.named("a spec like '0 * * * Sun'") | special.named("a named rule like '@daily'")
  }

  def whitespace: Rule0 = rule {
    quiet(zeroOrMore(anyOf(" \t")))
  }
}
