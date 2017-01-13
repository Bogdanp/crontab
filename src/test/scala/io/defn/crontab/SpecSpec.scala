package io.defn.crontab

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest._


class SpecSpec extends FunSpec with Matchers {
  describe("Spec") {
    describe("dateTimes") {
      it("should return a stream of upcoming ZonedDateTimes for a spec") {
        val start = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        val Right(spec) = Spec.parse("10-15 * * * Sun")
        val dateTimes = spec.dateTimes(start).take(10).toList

        dateTimes shouldEqual List(
          start.withHour(0).withMinute(10),
          start.withHour(0).withMinute(11),
          start.withHour(0).withMinute(12),
          start.withHour(0).withMinute(13),
          start.withHour(0).withMinute(14),
          start.withHour(0).withMinute(15),
          start.withHour(1).withMinute(10),
          start.withHour(1).withMinute(11),
          start.withHour(1).withMinute(12),
          start.withHour(1).withMinute(13)
        )
      }
    }
  }
}
