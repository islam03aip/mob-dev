package com.example.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.HabitUiState
import com.example.habittracker.ui.components.StatCard

@Composable
fun StatsScreen(state: HabitUiState, modifier: Modifier = Modifier) {
    val maxCount = (state.lastSevenDays.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Habits", state.total.toString(), Modifier.weight(1f))
            StatCard("Done today", state.doneToday.toString(), Modifier.weight(1f))
            StatCard("Best streak", state.bestStreak.toString(), Modifier.weight(1f))
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Completions, last 7 days", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    state.lastSevenDays.forEach { day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.count.toString(), style = MaterialTheme.typography.labelSmall)
                            Box(
                                Modifier
                                    .width(24.dp)
                                    .height((100.dp * day.count / maxCount).coerceAtLeast(4.dp))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(day.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
