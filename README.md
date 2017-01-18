# crontab [![Build Status](https://travis-ci.org/Bogdanp/crontab.svg?branch=master)](https://travis-ci.org/Bogdanp/crontab) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/0b04a1e33c1d4e4e95b794222c1a4020)](https://www.codacy.com/app/bogdan/crontab?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Bogdanp/crontab&amp;utm_campaign=Badge_Grade)

A crontab spec parser for Scala.

## Installation

``` scala
libraryDependencies += "io.defn" %% "crontab" % "0.1.0"
```

## Usage

``` scala
scala> import io.defn.crontab.Spec

scala> val Right(spec) = Spec.parse("*/2 * * * *")
spec: io.defn.crontab.Spec = Spec(Range(Minute(0),Minute(59),2),Every(),Every(),Every(),Every())

scala> val next10 = spec.dateTimes.take(10).toList
next10: List[java.time.ZonedDateTime] = List(2017-01-14T20:32+02:00[Europe/Bucharest], 2017-01-14T20:34+02:00[Europe/Bucharest], 2017-01-14T20:36+02:00[Europe/Bucharest], 2017-01-14T20:38+02:00[Europe/Bucharest], 2017-01-14T20:40+02:00[Europe/Bucharest], 2017-01-14T20:42+02:00[Europe/Bucharest], 2017-01-14T20:44+02:00[Europe/Bucharest], 2017-01-14T20:46+02:00[Europe/Bucharest], 2017-01-14T20:48+02:00[Europe/Bucharest], 2017-01-14T20:50+02:00[Europe/Bucharest])

scala> next10.foreach(println _)
2017-01-14T20:32+02:00[Europe/Bucharest]
2017-01-14T20:34+02:00[Europe/Bucharest]
2017-01-14T20:36+02:00[Europe/Bucharest]
2017-01-14T20:38+02:00[Europe/Bucharest]
2017-01-14T20:40+02:00[Europe/Bucharest]
2017-01-14T20:42+02:00[Europe/Bucharest]
2017-01-14T20:44+02:00[Europe/Bucharest]
2017-01-14T20:46+02:00[Europe/Bucharest]
2017-01-14T20:48+02:00[Europe/Bucharest]
2017-01-14T20:50+02:00[Europe/Bucharest]
```

### Warning

Certain specs could cause `dateTimes` to loop infinitely so if you
accept user input make sure you wrap the call in a `Future` and set a
timeout.
