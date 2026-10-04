import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.collections.first

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    val genre: String,
    val copies: Int
)

interface Describable {
    fun describe(): String
}

open class Member(val name: String, val maxBooks: Int) : Describable {
    val borrowed: MutableList<Book> = mutableListOf()

    fun canBorrow(book: Book): Boolean = borrowed.size < maxBooks

    override fun describe(): String = "$name (Limit: $maxBooks books)"
}

class StudentMember(name: String) : Member(name, maxBooks = 2) {
    override fun describe(): String = "Student ${super.describe()}"
}

class TeacherMember(name: String) : Member(name, maxBooks = 5) {
    override fun describe(): String = "Teacher ${super.describe()}"
}

sealed class LoanResult  {
    data class Success(val book: Book, val member: Member) : LoanResult()
    data class NoCopiesLeft(val book: Book) : LoanResult()
    data class LimitReached(val member: Member) : LoanResult()
    object BookNotFound : LoanResult()
}

class Library: Describable {
    private val books: MutableMap<Int, Book> = mutableMapOf()
    private val available: MutableMap<Int, Int> = mutableMapOf()
    val members: MutableList<Member> = mutableListOf()

    fun addBook(book: Book) {
        books[book.id] = book
        available[book.id] = book.copies
    }

    fun allBooks(): List<Book> = books.values.toList()

    fun genres(): Set<String> = books.values.map { it.genre }.toSet()

    fun availableCopies(bookId: Int): Int = available[bookId] ?: 0

    fun findBooks(predicate: (Book) -> Boolean): List<Book> = books.values.filter(predicate)

    suspend fun borrowBook(member: Member, bookId: Int): LoanResult {
        val book = books[bookId] ?: return LoanResult.BookNotFound
        val left = available[bookId] ?: 0

        return when {
            !member.canBorrow(book) -> LoanResult.LimitReached(member)
            left <= 0 -> LoanResult.NoCopiesLeft(book)
            else -> {
                available[bookId] = left - 1
                member.borrowed.add(book)
                LoanResult.Success(book, member)
            }
        }
    }

    override fun describe(): String = "Library with ${books.size} titles and ${members.size} members"
}

fun section(title: String, block: () -> Unit) {
    println("\n=== $title ===")
    block()
}

fun printResult(result: LoanResult) {
    val message = when (result) {
        is LoanResult.Success -> "OK: ${result.member.name} borrowed \"${result.book.title}\""
        is LoanResult.NoCopiesLeft -> "FAILED: No copies left of \"${result.book.title}\""
        is LoanResult.LimitReached -> "FAILED: \"${result.member.name}\" reached the limit"
        LoanResult.BookNotFound -> "FAILED: Book not found"
    }
    println(message)
}


fun main() = runBlocking {
    val library = Library()
    val libraryName = "City Library"
    var nextId = 1

    val sampleData = listOf(
        listOf("The Hobbit", "J.R.R. Tolkien", 1937, "Fantasy", 3),
        listOf("Dune", "Frank Herbert", 1965, "Sci-Fi", 2),
        listOf("Thomas' Calculus", "George B. Thomas, Jr.", 2013, "Calculus", 7)
    )

    for (row in sampleData) {
        library.addBook(
            Book(
                id = nextId++,
                title = row[0] as String,
                author = row[1] as String,
                year = row[2] as Int,
                genre = row[3] as String,
                copies = row[4] as Int
            )
        )
    }

    val alice = StudentMember("Alice")
    val bob = StudentMember("Bob")
    val carl = StudentMember("Carl")

    library.members.addAll(listOf(alice, bob, carl))

    println("Welcome to $libraryName")
    println(library.describe())

    section("Members (polymorphism)") {
        library.members.forEach { println(it.describe()) }
    }

    section("Collections: map, filter, reduce") {
        val books = library.allBooks()
        val titles = books.map { it.title }
        println("Titles: $titles")

        val oldBooks = library.findBooks { it.year < 1950 }
        println("Books before 1950: $oldBooks")

        val totalCOpies = books.map { it.copies }.reduce { sum, c -> sum + c }
        println("Total copies: $totalCOpies")

        println("Genres (Set): ${library.genres()}")

        val byGenre = books.groupBy { it.genre }
        for ((genre, list) in byGenre) {
            println("$genre -> ${list.size} title(s)")
        }
    }

    section("Borrowing books (coroutines)") {
        val requests = listOf(
            alice to 1, bob to 1, carl to 1,
            alice to 2, alice to 3,
            bob to 99
        )

        val results = runBlocking {
            requests.map {(member, bookId) ->
                async {library.borrowBook(member, bookId) }
            }.awaitAll()
        }
        results.forEach{ printResult(it) }
    }

    section("Statistics") {
        var i = 0
        while(i < library.members.size) {
            val m = library.members[i]
            println("${m.name} has: ${m.borrowed.map { it.title }}")
            i++
        }

        library.allBooks().forEach {
            println("${it.title}: ${library.availableCopies(it.id)}/${it.copies} available")
        }
    }
    val first = library.allBooks().first()
    println("\nData class demo: ${first.copy(title = "Thomas' Calculus (14th edition)")}")
}