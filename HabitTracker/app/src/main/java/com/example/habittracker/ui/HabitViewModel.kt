package com.example.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.model.Habit
import com.example.habittracker.model.HabitFilter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitItem(val habit: Habit, val doneToday: Boolean, val streak: Int)

data class DayCount(val label: String, val count: Int)

data class HabitUiState(
    val items: List<HabitItem> = emptyList(),
    val filter: HabitFilter = HabitFilter.ALL,
    val total: Int = 0,
    val doneToday: Int = 0,
    val bestStreak: Int = 0,
    val lastSevenDays: List<DayCount> = emptyList()
)

class HabitViewModel : ViewModel() {
    private val repository = HabitRepository()
    private val filter = MutableStateFlow(HabitFilter.ALL)
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<HabitUiState> = combine(repository.habits, filter) { habits, currentFilter ->
        val today = LocalDate.now()
        val all = habits.map { HabitItem(it, it.isDoneOn(today), it.streak(today)) }
        HabitUiState(
            items = all.filter {
                when (currentFilter) {
                    HabitFilter.ALL -> true
                    HabitFilter.PENDING -> !it.doneToday
                    HabitFilter.DONE -> it.doneToday
                }
            },
            filter = currentFilter,
            total = all.size,
            doneToday = all.count { it.doneToday },
            bestStreak = all.maxOfOrNull { it.streak } ?: 0,
            lastSevenDays = (6 downTo 0).map { offset ->
                val day = today.minusDays(offset.toLong())
                DayCount(
                    label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    count = habits.count { it.isDoneOn(day) }
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitUiState())

    fun setFilter(newFilter: HabitFilter) {
        filter.value = newFilter
    }

    fun addHabit(name: String, goal: String) {
        viewModelScope.launch {
            repository.add(name, goal)
            _messages.emit("Habit added")
        }
    }

    fun toggleToday(id: Int) {
        repository.toggle(id, LocalDate.now())
    }

    fun deleteHabit(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
            _messages.emit("Habit deleted")
        }
    }
}
