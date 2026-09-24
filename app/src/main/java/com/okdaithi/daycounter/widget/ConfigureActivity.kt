package com.okdaithi.daycounter.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.okdaithi.daycounter.MainActivity
import com.okdaithi.daycounter.R
import com.okdaithi.daycounter.core.Counter
import com.okdaithi.daycounter.core.CounterFormat
import com.okdaithi.daycounter.core.CounterMath
import com.okdaithi.daycounter.data.CounterRepository
import com.okdaithi.daycounter.ui.NocturneEasing
import com.okdaithi.daycounter.ui.WidgetPreview
import com.okdaithi.daycounter.ui.theme.Inter
import com.okdaithi.daycounter.ui.theme.Nocturne
import com.okdaithi.daycounter.ui.theme.NocturneTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * APPWIDGET_CONFIGURE: "Choose a counter" sheet over the launcher. Also runs on reconfigure
 * (Android 12+), where it adds an "Edit counter" shortcut for the currently bound counter.
 */
class ConfigureActivity : ComponentActivity() {

    private val appWidgetId by lazy {
        intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Backing out must not place the widget.
        setResult(RESULT_CANCELED, resultIntent())
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val repository = CounterRepository(this)
        val counters = repository.counters
        val boundId = repository.bindings.map { it[appWidgetId] }

        setContent {
            NocturneTheme {
                val list by counters.collectAsStateWithLifecycle(initialValue = null)
                val current by boundId.collectAsStateWithLifecycle(initialValue = null)
                ChooseCounterSheet(
                    counters = list,
                    currentId = current,
                    onPick = ::bind,
                    onEditCurrent = { id -> openEditor(id) },
                    onDismiss = { finish() },
                )
            }
        }
    }

    private fun bind(counter: Counter) {
        lifecycleScope.launch {
            CounterRepository(this@ConfigureActivity).bind(appWidgetId, counter.id)
            WidgetUpdater.updateAll(this@ConfigureActivity)
            setResult(RESULT_OK, resultIntent())
            finish()
        }
    }

    private fun openEditor(counterId: String) {
        // Reconfiguring keeps the widget either way, so report success.
        setResult(RESULT_OK, resultIntent())
        startActivity(MainActivity.editIntent(this, counterId))
        finish()
    }

    private fun resultIntent() = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}

@Composable
private fun ChooseCounterSheet(
    counters: List<Counter>?,
    currentId: String?,
    onPick: (Counter) -> Unit,
    onEditCurrent: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val today = remember { LocalDate.now() }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = shown, enter = fadeIn(tween(200))) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Nocturne.Scrim)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            )
        }
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically(tween(260, easing = NocturneEasing)) { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(Nocturne.Surface)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 40.dp),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 14.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .background(Nocturne.Neutral700, RoundedCornerShape(2.dp)),
                )
                Text(
                    "Choose a counter",
                    color = Nocturne.Text,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                if (currentId != null && counters?.any { it.id == currentId } == true) {
                    SheetAction(R.drawable.ic_pencil, "Edit counter") { onEditCurrent(currentId) }
                }
                if (counters != null && counters.isEmpty()) {
                    Text(
                        "No counters yet. Create one in the Counter app first.",
                        color = Nocturne.Neutral400,
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
                LazyColumn(
                    Modifier.heightIn(max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(counters.orEmpty(), key = { it.id }) { c ->
                        PickRow(c, today, selected = c.id == currentId) { onPick(c) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetAction(icon: Int, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(icon), null, tint = Nocturne.Text, modifier = Modifier.size(18.dp))
        Text(label, color = Nocturne.Text, fontFamily = Inter, fontSize = 14.sp)
    }
}

@Composable
private fun PickRow(counter: Counter, today: LocalDate, selected: Boolean, onClick: () -> Unit) {
    val r = CounterMath.calc(counter, today)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Nocturne.Text.copy(alpha = 0.06f) else Nocturne.Surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WidgetPreview(WidgetFace.of(counter, r), shapeSize = 47.6.dp) // 0.7×
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                counter.title,
                color = Nocturne.Text,
                fontFamily = Inter,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(CounterFormat.subtitle(counter, r), color = Nocturne.Neutral400, fontFamily = Inter, fontSize = 12.sp)
        }
    }
}
