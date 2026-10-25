package com.example.habittracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habittracker.ui.HabitViewModel
import com.example.habittracker.ui.screens.AddHabitScreen
import com.example.habittracker.ui.screens.HabitsScreen
import com.example.habittracker.ui.screens.StatsScreen
import androidx.compose.foundation.layout.padding

object Routes {
    const val HABITS = "habits"
    const val STATS = "stats"
    const val ADD = "add"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HABITS, "Habits", Icons.Default.CheckCircle),
    Tab(Routes.STATS, "Stats", Icons.Default.DateRange)
)

@Composable
fun HabitApp(viewModel: HabitViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (tabs.any { it.route == currentRoute }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.HABITS) {
                FloatingActionButton(onClick = { navController.navigate(Routes.ADD) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add habit")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HABITS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HABITS) {
                HabitsScreen(
                    state = state,
                    onFilter = viewModel::setFilter,
                    onToggle = viewModel::toggleToday,
                    onDelete = viewModel::deleteHabit
                )
            }
            composable(Routes.STATS) {
                StatsScreen(state = state)
            }
            composable(Routes.ADD) {
                AddHabitScreen(
                    onSave = { name, goal ->
                        viewModel.addHabit(name, goal)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
