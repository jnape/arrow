package arrow.test.laws

import arrow.Kind
import arrow.effects.Timer
import arrow.effects.typeclasses.Async
import arrow.effects.typeclasses.milliseconds
import arrow.effects.typeclasses.seconds
import arrow.test.generators.intSmall
import arrow.typeclasses.Eq
import io.kotlintest.properties.Gen

object TimerLaws {

  // TODO move to Arrow-effects and figure out a acceptable API for timeMilis/timeNano for MPP.
  interface Clock<F> {
    fun timeMillis(): Kind<F, Long>
    fun timeNano(): Kind<F, Long>

    companion object {
      operator fun <F> invoke(AS: Async<F>): Clock<F> = object : Clock<F> {
        override fun timeMillis(): Kind<F, Long> =
          AS.effect { System.currentTimeMillis() }
        override fun timeNano(): Kind<F, Long> =
          AS.effect { System.nanoTime() }
      }
    }
  }

  fun <F> laws(AS: Async<F>, T: Timer<F>, EQ: Eq<Kind<F, Boolean>>): List<Law> =
    listOf(
      Law("Timer Laws: sleep should last specified time") { AS.sleepShouldLastSpecifiedTime(T, Clock(AS), EQ) },
      Law("Timer Laws: negative sleep should be immediate") { AS.negativeSleepShouldBeImmediate(T, EQ) }
    )

  fun <F> Async<F>.sleepShouldLastSpecifiedTime(
    T: Timer<F>,
    C: Clock<F>,
    EQ: Eq<Kind<F, Boolean>>
  ) = forFew(25, Gen.intSmall()) {
    val lhs = binding {
      val start = !C.timeNano()
      !T.sleep(10.milliseconds)
      val end = !C.timeNano()
      (end - start) >= 10L
    }

    lhs.equalUnderTheLaw(just(true), EQ)
  }

  fun <F> Async<F>.negativeSleepShouldBeImmediate(
    T: Timer<F>,
    EQ: Eq<Kind<F, Boolean>>
  ) {
    T.sleep((-10).seconds)
      .map { it == Unit }
      .equalUnderTheLaw(just(true), EQ)
  }
}
