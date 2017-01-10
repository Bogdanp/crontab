package io.defn.crontab

import org.parboiled2._


private class CronSpecParser(val input: ParserInput) extends Parser {
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

  val minute  = () => rule { number(0, 60) ~> (Minute(_)) }
  val hour    = () => rule { number(0, 24) ~> (Hour(_)) }
  val day     = () => rule { number(1, 32) ~> (Day(_)) }
  val month   = () => rule { monthDigit | monthLiteral }
  val weekday = () => rule { weekdayDigit | weekdayLiteral }

  def every[T]: Rule1[Field[T]] = rule {
    ch('*') ~ push(Every())
  }

  def exact[T](inner: Meta[T]) = rule {
    inner() ~> (Exact(_))
  }

  def range[T](inner: Meta[T]) = rule {
    inner() ~ '-' ~ inner() ~> (Range(_, _))
  }

  def step[T](inner: Meta[T]) = rule {
    (range(inner) | every[T]) ~ '/' ~!~ number ~> (Step(_, _))
  }

  def component[T](inner: Meta[T]) = rule {
    range(inner) | exact(inner)
  }

  def sequence[T](inner: Meta[T]) = rule {
    (component(inner)) ~ ',' ~!~ oneOrMore(component(inner)).separatedBy(',') ~> {
      (head: Field[T], tail: Seq[Field[T]]) =>

      Sequence(head +: tail)
    }
  }

  def field[T](inner: Meta[T]) = rule {
    ( step(inner).named("a step")
      | every[T].named("*")
      | sequence(inner).named("a comma-separated sequence")
      | range(inner).named("a range")
      | exact(inner).named("an exact value")
    ) ~ whitespace
  }

  def spec: Rule1[CronSpec] = rule {
    ( whitespace
      ~ field(minute).named("a minute field")
      ~ field(hour).named("an hour field")
      ~ field(day).named("a day field")
      ~ field(month).named("a month field")
      ~ field(weekday).named("a weekday field")
      ~ EOI.named("the end of input")
    ) ~> {
      (minute: Field[Minute], hour: Field[Hour], day: Field[Day], month: Field[Month], weekday: Field[Weekday]) =>

      CronSpec(minute, hour, day, month, weekday)
    }
  }

  def yearly: Rule1[CronSpec] = rule {
    (ignoreCase("yearly") | ignoreCase("annually")) ~
      push(CronSpec(Exact(Minute(0)), Exact(Hour(0)), Exact(Day(1)), Exact(Jan), Every[Weekday]))
  }

  def monthly: Rule1[CronSpec] = rule {
    ignoreCase("monthly") ~
      push(CronSpec(Exact(Minute(0)), Exact(Hour(0)), Exact(Day(1)), Every[Month], Every[Weekday]))
  }

  def weekly: Rule1[CronSpec] = rule {
    ignoreCase("weekly") ~
      push(CronSpec(Exact(Minute(0)), Exact(Hour(0)), Every[Day], Every[Month], Exact(Sun)))
  }

  def daily: Rule1[CronSpec] = rule {
    (ignoreCase("daily") | ignoreCase("midnight")) ~
      push(CronSpec(Exact(Minute(0)), Exact(Hour(0)), Every[Day], Every[Month], Every[Weekday]))
  }

  def hourly: Rule1[CronSpec] = rule {
    ignoreCase("hourly") ~
      push(CronSpec(Exact(Minute(0)), Every[Hour], Every[Day], Every[Month], Every[Weekday]))
  }

  def special: Rule1[CronSpec] = rule {
    '@' ~!~ (yearly | monthly | weekly | daily | hourly) ~ EOI.named("the end of input")
  }

  def toplevel: Rule1[CronSpec] = rule {
    spec.named("a spec like '0 * * * Sun'") | special.named("a named rule like '@daily'")
  }

  def whitespace: Rule0 = rule {
    quiet(zeroOrMore(anyOf(" \t")))
  }
}
