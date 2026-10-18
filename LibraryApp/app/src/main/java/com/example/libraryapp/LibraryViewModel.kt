package com.example.libraryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_LOANS = 3

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val borrowedIds: Set<Int> = emptySet(),
    val message: String? = null,
    val isBusy: Boolean = false,
    val viewModelTaps: Int = 0
)

class LibraryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(books = sampleBooks()))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var nextId = 100

    fun borrow(bookId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            delay(400)

            val state = _uiState.value
            val book = state.books.firstOrNull { it.id == bookId }
            if (book == null) {
                _uiState.update { it.copy(isBusy = false) }
                return@launch
            }

            val result: LoanResult = when {
                bookId in state.borrowedIds -> LoanResult.AlreadyBorrowed(book)
                state.borrowedIds.size >= MAX_LOANS -> LoanResult.LimitReached
                book.available <= 0 -> LoanResult.NoCopiesLeft(book)
                else -> LoanResult.Success(book)
            }

            _uiState.update { s ->
                when (result) {
                    is LoanResult.Success -> s.copy(
                        books = s.books.map {
                            if (it.id == bookId) it.copy(available = it.available - 1) else it
                        },
                        borrowedIds = s.borrowedIds + bookId,
                        message = "Borrowed \"${result.book.title}\"",
                        isBusy = false
                    )
                    is LoanResult.NoCopiesLeft ->
                        s.copy(message = "No copies left of \"${result.book.title}\"", isBusy = false)
                    is LoanResult.AlreadyBorrowed ->
                        s.copy(message = "You already borrowed \"${result.book.title}\"", isBusy = false)
                    LoanResult.LimitReached ->
                        s.copy(message = "Limit reached ($MAX_LOANS books)", isBusy = false)
                }
            }
        }
    }

    fun returnBook(bookId: Int) {
        _uiState.update { s ->
            if (bookId !in s.borrowedIds) s
            else s.copy(
                books = s.books.map {
                    if (it.id == bookId) it.copy(available = it.available + 1) else it
                },
                borrowedIds = s.borrowedIds - bookId,
                message = "Book returned"
            )
        }
    }

    fun addBook(title: String, author: String, year: Int, genre: String, copies: Int) {
        val book = Book(nextId++, title, author, year, genre, copies)
        _uiState.update { it.copy(books = it.books + book, message = "Added \"$title\"") }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun tapViewModelCounter() = _uiState.update { it.copy(viewModelTaps = it.viewModelTaps + 1) }

    private fun sampleBooks() = listOf(
        Book(1, "1984", "George Orwell", 1949, "Dystopian", 2),
        Book(2, "Brave New World", "Aldous Huxley", 1932, "Dystopian", 1),
        Book(3, "The Hobbit", "J.R.R. Tolkien", 1937, "Fantasy", 3),
        Book(4, "Dune", "Frank Herbert", 1965, "Sci-Fi", 2),
        Book(5, "Clean Code", "Robert C. Martin", 2008, "Programming", 1)
    )
}

fun filterBooks(
    books: List<Book>,
    query: String,
    genre: String,
    onlyAvailable: Boolean
): List<Book> = books
    .filter { genre == "All" || it.genre == genre }
    .filter { !onlyAvailable || it.available > 0 }
    .filter {
        query.isBlank() ||
            it.title.contains(query, ignoreCase = true) ||
            it.author.contains(query, ignoreCase = true)
    }
