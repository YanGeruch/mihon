package eu.kanade.tachiyomi.ui.reader.viewer

import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences.ReaderTransitionAnimation

/**
 * Resolves a [ReaderTransitionAnimation] into the concrete [Interpolator] and duration each viewer
 * applies. Every animation is a cubic Bézier curve plus a duration: the presets use fixed curves
 * (with user-tunable durations), [ReaderTransitionAnimation.CUSTOM] uses a user-defined curve.
 *
 * [ResolvedTransition.interpolator] is `null` for [ReaderTransitionAnimation.DEFAULT], signalling the
 * viewer to keep its native transition (ViewPager settle / RecyclerView smooth scroll).
 */
object ReaderTransitionAnimations {

    // Ease-out — responsive start, soft landing.
    val SMOOTH_CURVE = floatArrayOf(0.25f, 0.1f, 0.25f, 1f)

    // Ease-in-out — soft start and stop.
    val GENTLE_CURVE = floatArrayOf(0.586f, 0.085f, 0.342f, 0.893f)

    fun resolve(
        animation: ReaderTransitionAnimation,
        smoothDurationMs: Int,
        gentleDurationMs: Int,
        customDurationMs: Int,
        customCurve: String,
    ): ResolvedTransition = when (animation) {
        ReaderTransitionAnimation.DEFAULT -> ResolvedTransition(null, 0)
        ReaderTransitionAnimation.SMOOTH -> ResolvedTransition(interpolatorOf(SMOOTH_CURVE), smoothDurationMs)
        ReaderTransitionAnimation.GENTLE -> ResolvedTransition(interpolatorOf(GENTLE_CURVE), gentleDurationMs)
        ReaderTransitionAnimation.CUSTOM -> ResolvedTransition(interpolatorOf(parseCurve(customCurve)), customDurationMs)
    }

    fun interpolatorOf(curve: FloatArray): Interpolator =
        PathInterpolator(curve[0].coerceIn(0f, 1f), curve[1], curve[2].coerceIn(0f, 1f), curve[3])

    /**
     * Parses a "x1,y1,x2,y2" string into Bézier control points, falling back to [SMOOTH_CURVE] when
     * malformed. The X coordinates are clamped to [0, 1] (required by [PathInterpolator]).
     */
    fun parseCurve(value: String): FloatArray {
        val parts = value.split(',').mapNotNull { it.trim().toFloatOrNull() }
        if (parts.size != 4) return SMOOTH_CURVE
        return floatArrayOf(parts[0].coerceIn(0f, 1f), parts[1], parts[2].coerceIn(0f, 1f), parts[3])
    }

    fun formatCurve(curve: FloatArray): String =
        curve.joinToString(",") { (Math.round(it * 1000f) / 1000f).toString() }
}

data class ResolvedTransition(
    val interpolator: Interpolator?,
    val durationMs: Int,
)
