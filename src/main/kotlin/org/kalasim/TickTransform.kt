package org.kalasim

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

// https://stackoverflow.com/questions/32437550/whats-the-difference-between-instant-and-localdatetime
interface TickTransform {
    fun tick2wallTime(tickTime: Double): Instant
    fun wall2TickTime(instant: Instant): Number
    fun durationAsTicks(duration: Duration): Double
}

class OffsetTransform(val offset: Instant = Instant.now(), val tickUnit: TimeUnit = TimeUnit.MINUTES) : TickTransform {
    override fun tick2wallTime(tickTime: Double): Instant {
        val durationSinceOffset = when(tickUnit){
            TimeUnit.NANOSECONDS -> Duration.ofNanos(tickTime.toLong())
            TimeUnit.MICROSECONDS -> Duration.ofNanos((tickTime * 1000).toLong())
            TimeUnit.MILLISECONDS -> Duration.ofMillis(tickTime.toLong())
            TimeUnit.SECONDS -> Duration.ofSeconds(tickTime.toLong())
            TimeUnit.MINUTES -> Duration.ofMinutes(tickTime.toLong())
            TimeUnit.HOURS -> Duration.ofHours(tickTime.toLong())
            TimeUnit.DAYS -> Duration.ofDays(tickTime.toLong())
        }

        return offset + durationSinceOffset
    }

    override fun wall2TickTime(instant: Instant): Number {
        val offsetDuration = Duration.between(offset, instant)

        return durationAsTicks(offsetDuration)
    }

    // todo improve precision of transformation
    override fun durationAsTicks(duration: Duration): Double = when(tickUnit){
        TimeUnit.NANOSECONDS -> duration.toNanos()
        TimeUnit.MICROSECONDS -> duration.toMillis()*1000
        TimeUnit.MILLISECONDS -> duration.toMillis()
        TimeUnit.SECONDS -> duration.toSeconds()
        TimeUnit.MINUTES -> duration.toMinutes()
        TimeUnit.HOURS -> duration.toHours()
        TimeUnit.DAYS -> duration.toDays()
    }.toDouble()
}

/** Transforms a simulation time (typically `now`) to the corresponding wall time. */
fun Environment.asWallTime(tickTime: Double) = tickTransform!!.tick2wallTime(tickTime)

/** Transforms a wall `duration` into the corresponding amount of ticks.*/
fun Environment.asTickDuration(duration: Duration) = tickTransform!!.durationAsTicks(duration)

/** Transforms an wall `Instant` to simulation time.*/
fun Environment.asTickTime(instant: Instant) = tickTransform!!.wall2TickTime(instant)