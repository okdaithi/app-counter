package com.okdaithi.daycounter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.okdaithi.daycounter.ui.AppViewModel
import com.okdaithi.daycounter.ui.CounterEditorScreen
import com.okdaithi.daycounter.ui.CounterListScreen
import com.okdaithi.daycounter.ui.EditorViewModel
import com.okdaithi.daycounter.ui.NocturneEasing
import com.okdaithi.daycounter.ui.NocturneToast
import com.okdaithi.daycounter.ui.theme.Nocturne
import com.okdaithi.daycounter.ui.theme.NocturneTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val startCounterId = if (savedInstanceState == null) intent.getStringExtra(EXTRA_COUNTER_ID) else null
        setContent {
            NocturneTheme {
                DayCounterNav(appViewModel, startCounterId)
            }
        }
    }

    companion object {
        const val EXTRA_COUNTER_ID = "counterId"

        /** Opens the app directly on a counter's editor (widget tap). */
        fun editIntent(context: Context, counterId: String): Intent =
            Intent(context, MainActivity::class.java)
                .setData(Uri.parse("daycounter://counter/$counterId"))
                .putExtra(EXTRA_COUNTER_ID, counterId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        fun listIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}

private const val ROUTE_LIST = "list"
private const val ROUTE_EDIT = "edit?${EditorViewModel.ARG_ID}={${EditorViewModel.ARG_ID}}"

private fun NavHostController.openEditor(id: String?) =
    navigate(if (id == null) "edit" else "edit?${EditorViewModel.ARG_ID}=${Uri.encode(id)}")

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DayCounterNav(appViewModel: AppViewModel, startCounterId: String?) {
    val nav = rememberNavController()
    val toast by appViewModel.toast.collectAsStateWithLifecycle()

    LaunchedEffect(startCounterId) {
        if (startCounterId != null) nav.openEditor(startCounterId)
    }

    Box(Modifier.fillMaxSize().background(Nocturne.Bg)) {
        NavHost(
            navController = nav,
            startDestination = ROUTE_LIST,
            // The list stays put; the editor slides over it from the right and back out.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.KeepUntilTransitionsFinished },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.KeepUntilTransitionsFinished },
        ) {
            composable(ROUTE_LIST) {
                val rows by appViewModel.rows.collectAsStateWithLifecycle()
                CounterListScreen(
                    rows = rows,
                    onOpen = { nav.openEditor(it) },
                    onNew = { nav.openEditor(null) },
                )
            }
            composable(
                ROUTE_EDIT,
                arguments = listOf(
                    navArgument(EditorViewModel.ARG_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
                enterTransition = { slideInHorizontally(tween(280, easing = NocturneEasing)) { it } },
                popExitTransition = { slideOutHorizontally(tween(280, easing = NocturneEasing)) { it } },
            ) {
                val vm: EditorViewModel = viewModel()
                val scope = rememberCoroutineScope()
                LaunchedEffect(vm.missing) { if (vm.missing) nav.popBackStack() }
                CounterEditorScreen(
                    isNew = vm.counterId == null,
                    draft = vm.draft,
                    errors = vm.errors,
                    onEdit = vm::edit,
                    onBack = { nav.popBackStack() },
                    onSave = {
                        scope.launch {
                            if (vm.save()) {
                                appViewModel.showToast("Saved")
                                nav.popBackStack(ROUTE_LIST, inclusive = false)
                            }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            vm.delete()
                            appViewModel.showToast("Counter deleted")
                            nav.popBackStack(ROUTE_LIST, inclusive = false)
                        }
                    },
                )
            }
        }

        NocturneToast(
            message = toast.message,
            visible = toast.visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 56.dp),
        )
    }
}
