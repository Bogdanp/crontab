package io.defn

/** ==Crontab==
  * Provides utilities for working with crontab time and date specifications.
  *
  * ===Overview===
  * The main class to use is [[Spec]].  It can be used to instantiate
  * valid crontab specs and to generate and match upcoming datetimes
  * that match it.  Its companion object provides a method that can be
  * used to parse crontab specifications.
  *
  * ====Parsing====
  * On success, a `Right` value containing the [[Spec]] is returned:
  *
  * {{{
  * scala> val Right(c) = Spec.parse("* * * * *")
  * c: Spec = Spec(Every(), Every(), Every(), Every(), Every())
  * }}}
  *
  * On failure, a `Left` value containing a parse error is returned:
  *
  * {{{
  * scala> val Left(error) = Spec.parse("foo")
  * error: String =
  * Invalid input 'f', expected a spec like '0 * * * Sun' or a named rule like '@daily' (line 1, column 1):
  * foo
  * ^
  * }}}
  *
  * ====Spec usage====
  * [[Spec Specs]] can be used to generate upcoming dates that match
  * from a given start time (defaults to the current time + 1 minute):
  *
  * {{{
  * scala> import io.defn.crontab._
  * import io.defn.crontab._
  *
  * scala> val Right(spec) = Spec.parse("0-59/2 * * * *")
  * spec: io.defn.crontab.Spec = Spec(Range(Minute(0),Minute(59),2),Every(),Every(),Every(),Every())
  *
  * scala> spec.dateTimes.take(2).toList.foreach(println _)
  * 2017-01-17T12:38+02:00[Europe/Bucharest]
  * 2017-01-17T12:40+02:00[Europe/Bucharest]
  * res0: ()
  * }}}
  *
  * They can also be used to check if specific dates match:
  *
  * {{{
  * scala> import io.defn.crontab._
  * import io.defn.crontab._
  *
  * scala> import java.time.{LocalDateTime, ZoneId}
  * import java.time.{LocalDateTime, ZoneId}
  *
  * scala> val now = LocalDateTime.now()
  * now: java.time.LocalDateTime = 2017-01-17T12:38:47.704
  *
  * scala> now.getDayOfWeek()
  * res0: java.time.DayOfWeek = TUESDAY
  *
  * scala> val Right(spec) = Spec.parse("* * * * Sun")
  * spec: io.defn.crontab.Spec = Spec(Every(),Every(),Every(),Every(),Exact(Sun))
  *
  * scala> spec.matches(now.atZone(ZoneId.of("UTC")))
  * res1: Boolean = false
  *
  * scala> spec.matches(now.plusDays(5).atZone(ZoneId.of("UTC")))
  * res2: Boolean = true
  * }}}
  *
  * ===Warning===
  * It is possible for certain specs to cause `dateTimes` to loop
  * infinitely (i.e. when no upcoming date times match the spec) so
  * handle user-input specs with extra care.
  */
package object crontab
