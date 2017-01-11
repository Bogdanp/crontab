package io.defn.crontab

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest._


class SpecSpec extends FunSpec with Matchers {
  describe("Spec") {
    describe("dateTimes") {
      it("should return a stream of upcoming ZonedDateTimes for a spec") {
        // val start = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        // val Right(spec) = Spec.parse("10-15 * * * Sun")

        // println(spec.dateTimes(start).take(10).toList)
      }
    }
  }
}
