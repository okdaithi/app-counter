package com.okdaithi.daycounter.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okdaithi.daycounter.ui.theme.Inter
import com.okdaithi.daycounter.ui.theme.Nocturne
import com.okdaithi.daycounter.widget.WidgetConfig
import com.okdaithi.daycounter.widget.WidgetFace
import com.okdaithi.daycounter.widget.WidgetRenderer
import com.okdaithi.daycounter.widget.WidgetStyle

/** Easing used across the app: cubic-bezier(.2, .8, .2, 1). */
val NocturneEasing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/** The widget face drawn at [shapeSize] (68dp is 1×). Shadow and glow may draw outside the bounds. */
@Composable
fun WidgetPreview(face: WidgetFace, shapeSize: Dp, modifier: Modifier = Modifier, style: WidgetStyle = WidgetConfig.style) {
    val context = LocalContext.current
    val renderer = remember { WidgetRenderer(context) }
    Canvas(modifier.size(shapeSize)) {
        drawIntoCanvas { renderer.draw(it.nativeCanvas, face, style, size.width, 0f, 0f) }
    }
}

/** Scales to 0.94 while pressed. */
fun Modifier.pressScale(pressed: Boolean): Modifier = graphicsLayer {
    val s = if (pressed) 0.94f else 1f
    scaleX = s
    scaleY = s
}

@Composable
fun IconButton36(@DrawableRes icon: Int, contentDescription: String, onClick: () -> Unit, iconSize: Dp = 22.dp) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "iconScale")
    Box(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(8.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription, tint = Nocturne.Text, modifier = Modifier.size(iconSize))
    }
}

/** Outlined primary button: never filled. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) Nocturne.Accent.copy(alpha = 0.22f) else Color.Transparent)
            .border(1.dp, Nocturne.Accent, RoundedCornerShape(8.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text, color = Nocturne.Accent, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun SecondaryButton(text: String, @DrawableRes icon: Int?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) Nocturne.Text.copy(alpha = 0.14f) else Color.Transparent)
            .border(1.dp, Nocturne.Divider, RoundedCornerShape(8.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(painterResource(icon), null, tint = Nocturne.Text, modifier = Modifier.size(16.dp))
        Text(text, color = Nocturne.Text, fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

/** 38×22 switch per the handoff spec, 150ms transition. */
@Composable
fun NocturneSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val spec = tween<Color>(150)
    val border by animateColorAsState(if (checked) Nocturne.Accent else Nocturne.Divider, spec, label = "border")
    val track by animateColorAsState(if (checked) Nocturne.Accent800 else Color.Transparent, spec, label = "track")
    val knob by animateColorAsState(if (checked) Nocturne.Accent else Nocturne.Neutral500, spec, label = "knob")
    val knobX by animateDpAsState(if (checked) 19.dp else 3.dp, tween(150), label = "knobX")
    Box(
        modifier = modifier
            .size(width = 38.dp, height = 22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(track)
            .border(1.dp, border, RoundedCornerShape(11.dp)),
    ) {
        Box(
            Modifier
                // Positions are inside the 1dp border, as in CSS.
                .offset(x = 1.dp + knobX, y = 4.dp)
                .size(14.dp)
                .background(knob, CircleShape),
        )
    }
}

/** Toast: bg neutral-800, 12.5sp, radius 18. Visibility and auto-hide are driven by the caller. */
@Composable
fun NocturneToast(message: String, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .background(Nocturne.Neutral800, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(message, color = Nocturne.Text, fontFamily = Inter, fontSize = 12.5.sp)
        }
    }
}

/** Field wrapper: 12sp label at 70% text opacity, 5dp gap, optional error below. */
@Composable
fun Field(label: String, error: String? = null, content: @Composable () -> Unit) {
    Column {
        Text(
            label,
            color = Nocturne.Text.copy(alpha = 0.7f),
            fontFamily = Inter,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 5.dp),
        )
        content()
        if (error != null) {
            Text(
                error,
                color = Nocturne.Accent300,
                fontFamily = Inter,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

/** Background mix toward the accent, used for pressed list rows (≈15% accent over surface). */
fun pressedSurface(pressed: Boolean): Color =
    if (pressed) lerp(Nocturne.Surface, Nocturne.Accent, 0.15f) else Nocturne.Surface
