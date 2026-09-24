package com.okdaithi.daycounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.okdaithi.daycounter.R
import com.okdaithi.daycounter.ui.theme.Inter
import com.okdaithi.daycounter.ui.theme.Nocturne

@Composable
fun CounterListScreen(
    rows: List<CounterRowUi>?,
    onOpen: (String) -> Unit,
    onNew: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Nocturne.Bg),
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                "Counters",
                color = Nocturne.Text,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp,
                letterSpacing = (-0.015).em,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 16.dp),
            )
            if (rows != null && rows.isEmpty()) {
                Text(
                    "No counters yet. Tap + to add one.",
                    color = Nocturne.Neutral400,
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rows.orEmpty(), key = { it.id }) { row -> CounterRow(row, onClick = { onOpen(row.id) }) }
            }
        }
        Fab(
            onClick = onNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 40.dp),
        )
    }
}

@Composable
private fun CounterRow(row: CounterRowUi, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(pressedSurface(pressed))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                row.title,
                color = Nocturne.Text,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(row.subtitle, color = Nocturne.Neutral400, fontFamily = Inter, fontSize = 12.sp)
            if (row.onHomeScreen) {
                Text(
                    "On home screen",
                    color = Nocturne.Neutral100,
                    fontFamily = Inter,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.02.em,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Nocturne.Neutral800, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 1.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                row.number,
                color = if (row.future) Nocturne.Accent300 else Nocturne.Text,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.03).em,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
            Text(row.caption, color = Nocturne.Neutral400, fontFamily = Inter, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Fab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .size(56.dp)
            .pressScale(pressed)
            .clip(shape)
            .background(lerp(Nocturne.Bg, Nocturne.Accent, if (pressed) 0.22f else 0.12f))
            .border(1.dp, Nocturne.Accent, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_plus), "New counter", tint = Nocturne.Accent, modifier = Modifier.size(24.dp))
    }
}
