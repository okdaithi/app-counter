package com.okdaithi.daycounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okdaithi.daycounter.R
import com.okdaithi.daycounter.core.CountUnit
import com.okdaithi.daycounter.core.CounterFormat
import com.okdaithi.daycounter.core.CounterMath
import com.okdaithi.daycounter.ui.theme.BodyStyle
import com.okdaithi.daycounter.ui.theme.Inter
import com.okdaithi.daycounter.ui.theme.Nocturne
import com.okdaithi.daycounter.widget.WidgetFace
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun CounterEditorScreen(
    isNew: Boolean,
    draft: Draft,
    errors: DraftErrors,
    onEdit: ((Draft) -> Draft) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Nocturne.Bg)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Row(
            Modifier.padding(start = 6.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconButton36(R.drawable.ic_arrow_left, "Back", onBack)
            Text(
                if (isNew) "New counter" else "Edit counter",
                color = Nocturne.Text,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton("Save", onSave)
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PreviewCard(draft)

            Field("Title", error = if (errors.title) "Add a title." else null) {
                NocturneInput(
                    value = draft.title,
                    onValueChange = { v -> onEdit { it.copy(title = v) } },
                    placeholder = "e.g. Trip to Japan",
                    singleLine = true,
                )
            }

            Field("Date", error = if (errors.date) "Pick a date." else null) {
                InputBox(onClick = { pickingDate = true }) {
                    Text(
                        draft.date?.let(CounterFormat::date) ?: "Pick a date",
                        style = BodyStyle,
                        color = if (draft.date == null) Nocturne.Neutral500 else Nocturne.Text,
                    )
                }
            }

            Field("Count in") {
                UnitSegments(draft.unit) { u -> onEdit { it.copy(unit = u) } }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ToggleRow("Include today", "Adds one day to the count", draft.includeToday) {
                    onEdit { it.copy(includeToday = !it.includeToday) }
                }
                ToggleRow("Repeat yearly", "Counts to the next anniversary", draft.repeatYearly) {
                    onEdit { it.copy(repeatYearly = !it.repeatYearly) }
                }
            }

            Field("Notes") {
                NocturneInput(
                    value = draft.notes,
                    onValueChange = { v -> onEdit { it.copy(notes = v) } },
                    placeholder = "Optional",
                    singleLine = false,
                    minHeight = 90.dp,
                )
            }

            if (!isNew) {
                SecondaryButton("Delete counter", R.drawable.ic_trash, onDelete)
            }
        }
    }

    if (pickingDate) {
        DateDialog(
            initial = draft.date ?: LocalDate.now(),
            onDismiss = { pickingDate = false },
            onPicked = { d ->
                pickingDate = false
                onEdit { it.copy(date = d) }
            },
        )
    }
}

@Composable
private fun PreviewCard(draft: Draft) {
    val today = LocalDate.now()
    val result = draft.date?.let { CounterMath.calc(it, draft.unit, draft.includeToday, draft.repeatYearly, today) }
    val face = result?.let {
        WidgetFace(CounterFormat.widgetNumber(it.n), CounterFormat.widgetSuffix(draft.unit), it.future)
    } ?: WidgetFace("0", CounterFormat.widgetSuffix(draft.unit), future = true)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(Nocturne.PreviewTop, Nocturne.Surface)))
                drawRect(
                    Brush.radialGradient(
                        0f to Nocturne.SectionGlow,
                        0.6f to Color.Transparent,
                        center = Offset(size.width * 0.15f, 0f),
                        radius = size.width * 1.2f,
                    ),
                )
            }
            .padding(top = 22.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        WidgetPreview(face, shapeSize = 102.dp) // 1.5×
        Text(
            CounterFormat.previewLine(draft.unit, draft.includeToday, result),
            color = Nocturne.Neutral300,
            fontFamily = Inter,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun InputBox(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Nocturne.Surface)
            .border(1.dp, Nocturne.Divider, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) { content() }
}

@Composable
private fun NocturneInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minHeight: Dp = 36.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = BodyStyle,
        cursorBrush = SolidColor(Nocturne.Accent),
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
                    .background(Nocturne.Surface, RoundedCornerShape(8.dp))
                    .border(1.dp, if (focused) Nocturne.Accent else Nocturne.Divider, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
            ) {
                if (value.isEmpty()) Text(placeholder, style = BodyStyle, color = Nocturne.Neutral500)
                inner()
            }
        },
    )
}

@Composable
private fun UnitSegments(selected: CountUnit, onPick: (CountUnit) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Nocturne.Divider, RoundedCornerShape(8.dp)),
    ) {
        CountUnit.entries.forEach { unit ->
            val isSelected = unit == selected
            Box(
                Modifier
                    .weight(1f)
                    .then(if (isSelected) Modifier.border(1.dp, Nocturne.Accent) else Modifier)
                    .clickable { onPick(unit) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    unit.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isSelected) Nocturne.Accent else Nocturne.Text,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, help: String, checked: Boolean, onFlip: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onFlip)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = BodyStyle)
            Text(help, color = Nocturne.Neutral400, fontFamily = Inter, fontSize = 12.sp)
        }
        NocturneSwitch(checked)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(initial: LocalDate, onDismiss: () -> Unit, onPicked: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
    )
    val colors = DatePickerDefaults.colors(
        containerColor = Nocturne.Surface,
        selectedDayContainerColor = Nocturne.Accent,
        selectedDayContentColor = Nocturne.Bg,
        todayDateBorderColor = Nocturne.Accent,
        todayContentColor = Nocturne.Accent,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()) else onDismiss()
            }) { Text("OK", color = Nocturne.Accent, fontFamily = Inter, fontWeight = FontWeight.Medium) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Nocturne.Accent, fontFamily = Inter, fontWeight = FontWeight.Medium)
            }
        },
        colors = colors,
    ) {
        DatePicker(state = state, colors = colors)
    }
}
