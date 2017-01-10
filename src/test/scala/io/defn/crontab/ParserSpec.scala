package io.defn.crontab

import org.scalatest._


class ParserSpec extends FunSpec with Matchers {
  describe("The parser") {
    it("should parse valid named rules") {
      val rules = Map(
        "@yearly"   -> Spec.yearly,
        "@annually" -> Spec.yearly,
        "@monthly"  -> Spec.monthly,
        "@weekly"   -> Spec.weekly,
        "@daily"    -> Spec.daily,
        "@midnight" -> Spec.daily,
        "@hourly"   -> Spec.hourly
      )

      for ((rule, value) <- rules)
        Spec.parse(rule) shouldEqual Right(value)
    }

    it("should fail to parse invalid named rules") {
      val Left(error) = Spec.parse("@foo")

      error should not be empty
    }
  }
}
