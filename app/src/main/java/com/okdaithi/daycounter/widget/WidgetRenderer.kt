package com.okdaithi.daycounter.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.okdaithi.daycounter.R
import com.okdaithi.daycounter.core.CounterFormat

/**
 * Draws the 1×1 widget in any of the three styles onto an Android [Canvas].
 * Used for the home-screen bitmap, the editor preview and the configuration sheet, so all three match.
 *
 * Geometry is in design units: the widget shape is 68×68, scaled to the requested size.
 */
class WidgetRenderer(context: Context) {

    private val inter: Typeface =
        runCatching { ResourcesCompat.getFont(context, R.font.inter_medium) }.getOrNull() ?: Typeface.DEFAULT

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val numberPaint = textPaint(letterSpacing = -0.04f)
    private val smallPaint = textPaint(letterSpacing = 0f)
    private val rect = RectF()

    private fun textPaint(letterSpacing: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = inter
        this.letterSpacing = letterSpacing
        fontFeatureSettings = "'tnum'"
    }

    /**
     * Renders a square bitmap of [sizePx]. The shape is inset by a margin so the Tile shadow and
     * the Ring glow are not clipped.
     */
    fun render(face: WidgetFace, style: WidgetStyle, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val shape = sizePx * SHAPE / CANVAS
        val inset = (sizePx - shape) / 2f
        draw(Canvas(bitmap), face, style, shape, inset, inset)
        return bitmap
    }

    /** Draws the widget with its shape occupying [left],[top] to +[shapePx]. Effects may draw outside. */
    fun draw(canvas: Canvas, face: WidgetFace, style: WidgetStyle, shapePx: Float, left: Float, top: Float) {
        val u = shapePx / SHAPE
        rect.set(left, top, left + shapePx, top + shapePx)
        when (style) {
            WidgetStyle.TILE -> drawTile(canvas, u)
            WidgetStyle.RING -> drawRing(canvas, u, face.future && !face.empty)
            WidgetStyle.SIGNED -> drawSigned(canvas, u)
        }
        drawNumber(canvas, face, style, u)
    }

    private fun drawTile(canvas: Canvas, u: Float) {
        val r = 22 * u
        // 0 4 12 rgba(0,0,0,.35); CSS blur 12 ≈ sigma 6 ≈ shadow radius 9.5
        fill.color = SURFACE
        fill.setShadowLayer(9.5f * u, 0f, 4 * u, 0x59000000)
        canvas.drawRoundRect(rect, r, r, fill)
        fill.clearShadowLayer()
        drawOuterRing(canvas, u, r)
    }

    private fun drawRing(canvas: Canvas, u: Float, glowing: Boolean) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = rect.width() / 2
        fill.color = NEUTRAL_900
        // 0 0 16 accent @ 40%; CSS blur 16 ≈ sigma 8 ≈ shadow radius 13
        if (glowing) fill.setShadowLayer(13f * u, 0f, 0f, ACCENT_40)
        canvas.drawCircle(cx, cy, radius, fill)
        fill.clearShadowLayer()
        stroke.strokeWidth = 2 * u
        stroke.color = if (glowing) ACCENT else NEUTRAL_800
        canvas.drawCircle(cx, cy, radius - u, stroke)
    }

    private fun drawSigned(canvas: Canvas, u: Float) {
        val r = 22 * u
        // Surface at 70% alpha. Backdrop blur is not available to widgets; the translucency stays.
        fill.color = SURFACE_70
        canvas.drawRoundRect(rect, r, r, fill)
        drawOuterRing(canvas, u, r)
    }

    /** CSS `box-shadow: 0 0 0 1px` ring: 1 unit, drawn just outside the shape. */
    private fun drawOuterRing(canvas: Canvas, u: Float, r: Float) {
        stroke.strokeWidth = u
        stroke.color = NEUTRAL_800
        val half = u / 2
        val ring = RectF(rect.left - half, rect.top - half, rect.right + half, rect.bottom + half)
        canvas.drawRoundRect(ring, r + half, r + half, stroke)
    }

    private fun drawNumber(canvas: Canvas, face: WidgetFace, style: WidgetStyle, u: Float) {
        val fs = CounterFormat.widgetFontSizeSp(face.number, face.suffix) * u
        val gap = u
        val numberColor = when {
            face.empty -> NEUTRAL_500
            style == WidgetStyle.TILE && face.future -> ACCENT_300
            else -> TEXT
        }

        numberPaint.textSize = fs
        numberPaint.color = numberColor
        val numberWidth = numberPaint.measureText(face.number)

        smallPaint.textSize = SUFFIX_SIZE * u
        val suffixWidth = if (face.suffix.isEmpty()) 0f else smallPaint.measureText(face.suffix)

        val sign = if (style == WidgetStyle.SIGNED && !face.empty) (if (face.future) "−" else "+") else ""
        smallPaint.textSize = SIGN_SIZE * u
        val signWidth = if (sign.isEmpty()) 0f else smallPaint.measureText(sign)

        val total = (if (sign.isEmpty()) 0f else signWidth + gap) + numberWidth +
            (if (face.suffix.isEmpty()) 0f else gap + suffixWidth)
        var x = rect.centerX() - total / 2

        // Line box of the number (line-height 1), centred vertically.
        val lineTop = rect.centerY() - fs / 2
        val numberBaseline = baselineFor(numberPaint, lineTop, fs)

        if (sign.isNotEmpty()) {
            smallPaint.textSize = SIGN_SIZE * u
            smallPaint.color = ACCENT_400
            // Top-aligned with a 1-unit offset.
            canvas.drawText(sign, x, baselineFor(smallPaint, lineTop + u, SIGN_SIZE * u), smallPaint)
            x += signWidth + gap
        }

        canvas.drawText(face.number, x, numberBaseline, numberPaint)
        x += numberWidth

        if (face.suffix.isNotEmpty()) {
            smallPaint.textSize = SUFFIX_SIZE * u
            smallPaint.color = (numberColor and 0x00FFFFFF) or (0xB3 shl 24) // 70% opacity
            val suffixBaseline = if (style == WidgetStyle.SIGNED) {
                // Bottom-aligned with the number's line box.
                baselineFor(smallPaint, lineTop + fs - SUFFIX_SIZE * u, SUFFIX_SIZE * u)
            } else {
                numberBaseline
            }
            canvas.drawText(face.suffix, x + gap, suffixBaseline, smallPaint)
        }
    }

    /** Baseline for text set with line-height equal to [size], given its line box [top]. */
    private fun baselineFor(paint: Paint, top: Float, size: Float): Float {
        val fm = paint.fontMetrics
        val ascent = -fm.ascent
        val halfLeading = (size - (ascent + fm.descent)) / 2
        return top + halfLeading + ascent
    }

    companion object {
        /** Widget shape and bitmap canvas, in design units. The margin leaves room for shadow and glow. */
        const val SHAPE = 68f
        const val CANVAS = 84f

        private const val SUFFIX_SIZE = 11f
        private const val SIGN_SIZE = 13f

        private const val SURFACE = 0xFF232532.toInt()
        private const val SURFACE_70 = 0xB3232532.toInt()
        private const val TEXT = 0xFFE9E9ED.toInt()
        private const val ACCENT = 0xFF9184D9.toInt()
        private const val ACCENT_40 = 0x669184D9
        private const val ACCENT_300 = 0xFFD2CEFD.toInt()
        private const val ACCENT_400 = 0xFFB5ABFC.toInt()
        private const val NEUTRAL_500 = 0xFF9397AB.toInt()
        private const val NEUTRAL_800 = 0xFF3F424D.toInt()
        private const val NEUTRAL_900 = 0xFF292B31.toInt()
    }
}
