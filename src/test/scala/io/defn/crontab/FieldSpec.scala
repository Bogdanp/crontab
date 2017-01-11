package io.defn.crontab

import org.scalatest._


class FieldSpec extends FunSpec with Matchers {
  import Field._

  describe("Every") {
    it("matches any value") {
      Every[Minute].matches(10) shouldBe true
    }
  }

  describe("Exact") {
    it("matches exact values") {
      Exact(Minute(10)).matches(10) shouldBe true
    }

    it("fails to match invalid values") {
      Exact(Minute(10)).matches(20) shouldBe false
    }
  }

  describe("Range") {
    it("matches ranges of values") {
      val range = Range(Minute(10), Minute(30))

      for (i <- 10 to 30)
        range.matches(i) shouldBe true

      range.matches(1) shouldBe false
    }

    it("matches ranges of values with steps") {
      val range = Range(Minute(0), Minute(59), 2)

      for (i <- 0 until 60)
        if (i % 2 == 0) range.matches(i) shouldBe true
        else range.matches(i) shouldBe false
    }

    it("matches ranges of weekdays") {
      val range = Range(Weekday.Mon, Weekday.Sun, 2)

      for (i <- 1 to 7) {
        if (i % 2 != 0) range.matches(i) shouldBe true
        else range.matches(i) shouldBe false
      }
    }

    it("matches ranges of values with weird steps") {
      val range = Range(Weekday.Mon, Weekday.Sun, 5)

      for (i <- 1 to 7) {
        if (Seq(1, 6) contains i) range.matches(i) shouldBe true
        else range.matches(i) shouldBe false
      }
    }
  }
}
