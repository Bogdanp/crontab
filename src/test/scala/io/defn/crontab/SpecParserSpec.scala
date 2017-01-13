package io.defn.crontab

import org.scalatest._


class SpecParserSpec extends FunSpec with Matchers {
  import Field._

  val namedRules = Map(
    "@yearly"   -> Spec.yearly,
    "@annually" -> Spec.yearly,
    "@monthly"  -> Spec.monthly,
    "@weekly"   -> Spec.weekly,
    "@daily"    -> Spec.daily,
    "@midnight" -> Spec.daily,
    "@hourly"   -> Spec.hourly
  )

  describe("Parsing") {
    describe("named rules") {
      it("should parse valid named rules") {
        for ((rule, value) <- namedRules)
          Spec.parse(rule) shouldEqual Right(value)
      }

      it("should parse valid uppercased named rules") {
        for ((rule, value) <- namedRules)
          Spec.parse(rule.toUpperCase) shouldEqual Right(value)
      }

      it("should fail to parse invalid named rules") {
        val Left(error) = Spec.parse("@foo")

        error should not be empty
      }
    }

    describe("specs") {
      it("should parse simple specs correctly") {
        Spec.parse("* * * * *") shouldEqual Right(Spec(
          Every[Minute], Every[Hour], Every[Day], Every[Month], Every[Weekday]))
      }

      it("should parse exact times correctly") {
        Spec.parse("15 14 * * *") shouldEqual Right(Spec(
          Exact(Minute(15)), Exact(Hour(14)), Every[Day], Every[Month], Every[Weekday]))
      }

      it("should parse exact months correctly") {
        Spec.parse("0 0 1 Jan *") shouldEqual Right(Spec(
          Exact(Minute(0)), Exact(Hour(0)), Exact(Day(1)), Exact[Month](Month.Jan), Every[Weekday]))
      }

      it("should parse exact weekdays correctly") {
        Spec.parse("* * * * Sun") shouldEqual Right(Spec(
          Every[Minute], Every[Hour], Every[Day], Every[Month], Exact[Weekday](Weekday.Sun)))
      }

      it("should parse asterisk-based steps correctly") {
        Spec.parse("*/2 * * * *") shouldEqual Right(Spec(
          Range(Minute(0), Minute(59), 2), Every[Hour], Every[Day], Every[Month], Every[Weekday]))
      }

      it("should parse range-based steps correctly") {
        Spec.parse("10-20/2 * * * *") shouldEqual Right(Spec(
          Range(Minute(10), Minute(20), 2), Every[Hour], Every[Day], Every[Month], Every[Weekday]))
      }

      it("should parse range-based steps of months correctly") {
        Spec.parse("* * * jan-sep/2 *") shouldEqual Right(Spec(
          Every[Minute], Every[Hour], Every[Day], Range[Month](Month.Jan, Month.Sep, 2), Every[Weekday]))
      }

      it("should parse sequences correctly") {
        Spec.parse("10,20,30 * * * *") shouldEqual Right(Spec(
          Sequence(Seq(Exact(Minute(10)), Exact(Minute(20)), Exact(Minute(30)))), Every[Hour], Every[Day], Every[Month], Every[Weekday]))
      }

      it("should fail to parse incomplete specs") {
        val message =
          """Unexpected end of input, expected '/' or a weekday field (line 1, column 8):
            |* * * *
            |       ^
          """.stripMargin.trim

        Spec.parse("* * * *") shouldEqual Left(message)
      }

      it("should fail to parse invalid specs") {
        val message =
          """Invalid input 'f', expected a spec like '0 * * * Sun' or a named rule like '@daily' (line 1, column 1):
            |foo
            |^
          """.stripMargin.trim

        Spec.parse("foo") shouldEqual Left(message)
      }

      it("should fail to parse invalid ranges") {
        val message =
          """Invalid input ' ', expected an increasing range, not 'Minute(10)-Minute(5)' (line 1, column 5):
            |10-5 * * * *
            |    ^
          """.stripMargin.trim

        Spec.parse("10-5 * * * *") shouldEqual Left(message)
      }

      it("should fail to parse invalid dates") {
        Spec.parse("* * 30 Feb *") shouldEqual Left("Invalid spec: day cannot be 30 for month Feb")
        Spec.parse("* * 31 Apr *") shouldEqual Left("Invalid spec: day cannot be 31 for month Apr")
      }

      it("should fail to parse invalid date ranges") {
        Spec.parse("* * 30-31 Feb *") shouldEqual Left("Invalid spec: range of days cannot be Range(Day(30),Day(31),1) for month Feb")
      }
    }
  }
}
