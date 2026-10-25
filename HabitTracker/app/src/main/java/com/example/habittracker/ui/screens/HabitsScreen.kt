package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.HabitFilter
import com.example.habittracker.ui.HabitUiState
import com.example.habittracker.ui.components.FilterRow
import com.example.habittracker.ui.components.HabitCard

@Composable
fun HabitsScreen(
    state: HabitUiState,
    onFilter: (HabitFilter) -> Unit,
    onToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (state.total == 0) 0f else state.doneToday.toFloat() / state.total

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Today", style = MaterialTheme.typography.headlineMedium)
            Text("${state.doneToday} of ${state.total} habits completed")
        }
        item {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row { FilterRow(selected = state.filter, onSelect = onFilter) }
        }
        if (state.items.isEmpty()) {
            item {
                Text(
                    "Nothing here yet. Tap + to add a habit.",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
        items(state.items, key = { it.habit.id }) { item ->
            HabitCard(
                item = item,
                onToggle = { onToggle(item.habit.id) },
                onDelete = { onDelete(item.habit.id) }
            )
        }
    }
}
