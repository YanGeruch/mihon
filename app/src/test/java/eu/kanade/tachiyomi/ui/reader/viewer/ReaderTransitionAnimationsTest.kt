package eu.kanade.tachiyomi.ui.reader.viewer

import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
class ReaderTransitionAnimationsTest {

    @Test
    fun `parseCurve reads four control points`() {
        ReaderTransitionAnimations.parseCurve("0.66,0,0.34,1").toList() shouldBe
            listOf(0.66f, 0f, 0.34f, 1f)
    }

    @Test
    fun `parseCurve trims whitespace around values`() {
        ReaderTransitionAnimations.parseCurve(" 0.25 , 0.1 , 0.25 , 1 ").toList() shouldBe
            listOf(0.25f, 0.1f, 0.25f, 1f)
    }

    @Test
    fun `parseCurve clamps x coordinates to 0 to 1 but leaves y free`() {
        // PathInterpolator requires x in [0, 1]; y is left untouched so overshoot curves survive.
        ReaderTransitionAnimations.parseCurve("-0.5,2,1.5,-1").toList() shouldBe
            listOf(0f, 2f, 1f, -1f)
    }

    @Test
    fun `parseCurve falls back to the smooth curve when malformed`() {
        val smooth = ReaderTransitionAnimations.SMOOTH_CURVE.toList()
        ReaderTransitionAnimations.parseCurve("not-a-curve").toList() shouldBe smooth
        ReaderTransitionAnimations.parseCurve("1,2,3").toList() shouldBe smooth // too few
        ReaderTransitionAnimations.parseCurve("1,2,3,4,5").toList() shouldBe smooth // too many
        ReaderTransitionAnimations.parseCurve("").toList() shouldBe smooth
    }

    @Test
    fun `formatCurve rounds to three decimals`() {
        ReaderTransitionAnimations.formatCurve(floatArrayOf(0.6666f, 0f, 0.3334f, 1f)) shouldBe
            "0.667,0.0,0.333,1.0"
    }

    @Test
    fun `formatCurve then parseCurve round-trips an in-range curve`() {
        val curve = floatArrayOf(0.25f, 0.1f, 0.25f, 1f)
        val restored = ReaderTransitionAnimations.parseCurve(ReaderTransitionAnimations.formatCurve(curve))
        restored.toList() shouldBe curve.toList()
    }

    @Test
    fun `resolve keeps the native transition for DEFAULT`() {
        val resolved = ReaderTransitionAnimations.resolve(
            animation = ReaderPreferences.ReaderTransitionAnimation.DEFAULT,
            smoothDurationMs = 500,
            gentleDurationMs = 1000,
            customDurationMs = 1000,
            customCurve = "0.66,0,0.34,1",
        )
        resolved.interpolator shouldBe null
        resolved.durationMs shouldBe 0
    }
}
