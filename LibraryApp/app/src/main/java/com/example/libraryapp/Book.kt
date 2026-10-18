package com.example.libraryapp

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    val genre: String,
    val totalCopies: Int,
    val available: Int = totalCopies
)

sealed class LoanResult {
    data class Success(val book: Book) : LoanResult()
    data class NoCopiesLeft(val book: Book) : LoanResult()
    data class AlreadyBorrowed(val book: Book) : LoanResult()
    object LimitReached : LoanResult()
}
