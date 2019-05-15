package arrow.benchmarks

import arrow.effects.IO
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.CompilerControl
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class Delay {

  @Param("3000")
  var size: Int = 0

  private fun ioDelayLoop(i: Int): IO<Int> =
    IO { i }.flatMap { j ->
      if (j > size) IO { j } else ioDelayLoop(j + 1)
    }

  @Benchmark
  fun io(): Int =
    IO.unsafeRunBlocking(ioDelayLoop(0))

  @Benchmark
  fun catsIO(): Int =
    arrow.benchmarks.effects.scala.cats.`Delay$`.`MODULE$`.unsafeIODelayLoop(size, 0)

  @Benchmark
  fun scalaZIO(): Int =
    arrow.benchmarks.effects.scala.zio.`Delay$`.`MODULE$`.unsafeIODelayLoop(size, 0)
}
