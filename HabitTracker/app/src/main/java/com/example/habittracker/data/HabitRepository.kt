package com.example.habittracker.data

import com.example.habittracker.model.Habit
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HabitRepository {
    private var nextId = 4
    private val _habits = MutableStateFlow(
        listOf(
            Habit(1, "Drink water", "8 glasses a day"),
            Habit(2, "Read", "20 pages"),
            Habit(3, "Walk", "30 minutes outside")
        )
    )
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    fun add(name: String, goal: String) {
        _habits.update { it + Habit(nextId++, name.trim(), goal.trim()) }
    }

    fun toggle(id: Int, date: LocalDate) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id != id) habit
                else habit.copy(
                    completedDates = if (date in habit.completedDates) {
                        habit.completedDates - date
                    } else {
                        habit.completedDates + date
                    }
                )
            }
        }
    }

    fun delete(id: Int) {
        _habits.update { list -> list.filterNot { it.id == id } }
    }
}
