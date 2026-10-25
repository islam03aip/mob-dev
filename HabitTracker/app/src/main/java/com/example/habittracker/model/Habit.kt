package com.example.habittracker.model

import java.time.LocalDate

data class Habit(
    val id: Int,
    val name: String,
    val goal: String,
    val completedDates: Set<LocalDate> = emptySet()
) {
    fun isDoneOn(date: LocalDate): Boolean = date in completedDates

    fun streak(today: LocalDate): Int {
        var day = if (isDoneOn(today)) today else today.minusDays(1)
        var count = 0
        while (isDoneOn(day)) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}

enum class HabitFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    DONE("Done")
}
