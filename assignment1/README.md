# LibraryApp

A small Kotlin console application that simulates a library. Books can be added,
searched, grouped and borrowed by different kinds of members (students and teachers).

## How to run
1. Clone this repository
2. Open IntelliJ IDEA => File => Open => select the `assignment1` folder (it will be imported as a Gradle project).
3. Wait until Gradle finishes syncing (it downloads the Kotlin Coroutines dependency).
4. Open `src/main/kotlin/Main.kt` and click the green run button next to `fun main()`.

Or from a terminal (requires JDK 17+):

```
./gradlew run
```
(on Windows: `gradlew.bat run`; if the wrapper is missing, use IntelliJ's run button.)

## Where the requirements are demonstrated (all in `Main.kt`)

| Requirement | Where |
|---|---|
| Variables, data types, conditions, loops | `main()` (`val`/`var`, `for`, `while`), `borrowBook()` (`when`, `if`) |
| List, Set, Map | `Library`: `members` (List), `genres()` (Set), `books` / `available` (Map) |
| map, filter, reduce | Section "Collections" in `main()`; `findBooks()` uses `filter` |
| Functions, higher-order functions, lambdas | `section(title, block)`, `findBooks(predicate)`, lambdas like `{ it.year < 1950 }` |
| Classes and objects | `Library`, `Member`, instances `alice`, `bob`, `carl` |
| Inheritance | `StudentMember` and `TeacherMember` extend `Member` |
| Interfaces and polymorphism | `Describable` interface; `describe()` overridden and called on a `List<Member>` |
| Data class | `Book` (also uses `copy()` at the end of `main()`) |
| Sealed class | `LoanResult` with `Success`, `NoCopiesLeft`, `LimitReached`, `BookNotFound`; handled with `when` in `printResult()` |
| Suspend function + coroutine | `Library.borrowBook()` is `suspend` (uses `delay`); `main()` uses `runBlocking`, `async` and `awaitAll` |
