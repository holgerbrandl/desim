## Clock Synchronization

In simulation a clear distinction is made between real time and simulation time. With *real time* we refer to the wall-clock time. It represents the execution time of the experiment. The *simulation time* is an attribute of the simulator.

To support use cases where a simulation may drive a demonstration or system check, the `kalasim` API allows to run a simulation at a defined clock speed. Such real-time simulations may be necessary

* If you have hardware-in-the-loop
* If the intent of the simulation is to drive a visualization of a process
* If there is human interaction with your simulation, or
* If you want to analyze the real-time behavior of an algorithm

```kotlin hl_lines="9"
{!api/RealtimeSimulation.kts!}
```
This example will execute in 10 seconds. Since the simulation is empty (for educational reasons to keep the focus on the clock here), it is entirely idle during that time.

To enable clock synchronization, we need to add a `ClockSync` to our simulation. We need to define what one _tick_ in simulation time corresponds to in wall time. In the example, one tick equals to one second wall time. This is configured with the parameter `tickDuration`. It defines the duration of a simulation tick in wall clock coordinates. It can be created with `Duration.ofSeconds(1)`, `Duration.ofMinutes(10)` and [so on](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Duration.html).

`ClockSync` also provides settings for more advanced uses-cases

* To run simulations, in more than realtime, the user can specify `speedUp` to run a simulation faster (`speedUp` > 1) or slower (`speedUp` < 1) than realtime. It defaults to `1`, that is no speed-up will be applied.
* The argument `syncsPerTick` defines how often a clock synchronization should happen. Per default it synchronizes once per _tick_ (i.e. an 1-increment of simulation time).


It may happen that a simulation is too complex to run at a defined clock. In such a situation, it (i.e. `Environment.run()`) will throw a `ClockOverloadException` if the user has specified a maximum delay `maxDelay` parameter between simulation and wall clock coordinates.


<!-- Inspired by <https://simpy.readthedocs.io/en/latest/topical_guides/real-time-simulations.html>-->

## Tick Transformation

Simulation time is measured in ticks. Usually, a simulation starts at `0` and then progresses through actions such as hold or wait or component generation.

To enable a more eye-friendly logging and to stay closer to the system under study, `kalasim` supports a built in transformation `tickTransform` to convert from simulation to wall clock. Let's consider the following example

```kotlin hl_lines="9"
{!api/TickTrafoExample.kts!}
```
This example will run for 2h in total which is transformed to 2x60 ticks, and will report a transform wall time of `now` plus 120 minutes. It also illustrates the 3 supported provided transformations:

* `asWallTime(tickTime)` - Transforms a simulation time (typically `now`) to the corresponding wall time.
* `asTickDuration(duration)` - Transforms a wall `duration` into the corresponding amount of ticks.
* `asTickTime(instant)` - Transforms an wall `Instant` to simulation time.
 

Please note that setting this transformation does not impact the actual simulation, which is always carried out in ticks. It can be configured independently from the [clock synchronization](#clock-synchronization) described above.

There is one provided implementation `OffsetTransform` that can be instantiated with  a start time offset the unit of a tick. The user can also implement own transformation by implementing the [functional interface](https://kotlinlang.org/docs/reference/fun-interfaces.html) `TickTransform`.

## Operational Control

**{Note}** This feature is still in preparation and planned for version v0.8, see <https://github.com/holgerbrandl/kalasim/issues/9> and [roadmap](https://github.com/holgerbrandl/kalasim/blob/master/docs/roadmap.md).

Even if `kalasim` tries to provide a simplistic declarative approach to define a simulation, it may come along with computational demands simulation. To allow introspection into time-complexity of the underlying computations, the user may want to use the built-in `env.tickMetrics` [monitor](monitors.md) to analyze how much time is spent per time unit (aka *tick*). This monitor is not enabled by default and need to enabled when the environment is created by passing `tickMetricsEnabled=true`

<!-- Also see https://cran.r-project.org/web/packages/simmer/vignettes/simmer-01-introduction.html#replication --> 

## Performance tuning

There are multiple ways to improve the performance of a simulation. 

* Set the correct `AssertMode`: The assertion mode determines which internal consistency checks are being performed.  The mode can be set to `Full` (Slowest), `Light` (default) or `Off` (Fastest). Depending on simulation logic and complexity, this will improve performance by ~20%. 
* Disable internal event logging: The [interaction model](component.md) is configured by default to provide insights into the simulation via the [event log](event_log.md). However, to optimize performance of a simulation a user may want to consume only custom event-types. If so, internal interaction logging can be disabled by setting `logCoreInteractions = false` when creating/configuring a [component](component.md).  
* Disable component statistics: Components and queues log various component statistics with built-in [monitors](monitors.md) which can be [disabled](monitors.md) to reduce compute and memory footprint of a simulation.   

To further fine-tune and optimize simulation performance and to reveal bottlenecks, a JVM profiler (such as [yourkit](https://www.yourkit.com/)) can be used. Both call-counts and spent-time analysis have been proven useful here. 

## Save and Load Simulations

<!-- TODO learn from https://github.com/r-simmer/simmer.json -->

`kalasim` does not include a default mechanism to serialize and deserialize simulations yet. However, it [seems](https://github.com/holgerbrandl/kalasim/blob/master/src/test/kotlin/org/kalasim/misc/SaveLoadSimulation.kt) that with [xstream](https://x-stream.github.io/) that `Environment` can be saved including its current simulation state across all included entities. It can be restored from the xml snapshot and continued with `run()`.

 We have not [succeeded](https://github.com/holgerbrandl/kalasim/blob/master/src/test/kotlin/org/kalasim/misc/SaveLoadSimulation.kt#L39) to do the same with [gson](https://github.com/google/gson) yet. Also, some experiments with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) were not that successful.