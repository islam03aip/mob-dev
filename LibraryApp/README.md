# Library App

An Android app.
It continues the HW1 console Library project: browse books, search, filter by genre,
borrow and return books, and add new books.

## How to run
1. Open the project in Android Studio and wait for Gradle sync.
2. Select an emulator or a connected device and click Run.

| Requirement | Where |
|---|---|
| Compose + Material 3 | All UI files (`Scaffold`, `TopAppBar`, `Card`, `FilterChip`, `Button`, ...) |
| 3+ reusable composables | `BookCard`, `SearchField`, `GenreFilterRow`, `OnlyAvailableRow`, `StateDemoCard`, `AddBookDialog` |
| Row / Column / Box | `BookCard` (Row, Column, Box), `LibraryScreen` (Column) |
| LazyColumn, Card, Text, Image | `LibraryScreen` (LazyColumn), `BookCard` (Card, Text, Image) |
| 2+ user interactions | Search, genre filter, only-available checkbox, borrow/return, add book |
| Input components | `OutlinedTextField`, `Button`, `Checkbox`, `FilterChip` |
| UI updates on actions | State is changed -> Compose recomposes the list |
| remember / rememberSaveable | `LibraryScreen` (search query, filter, counters) |
| ViewModel | `LibraryViewModel` (books, borrowed ids, messages) |
| UI separated from state logic | UI in `LibraryScreen.kt`, logic in `LibraryViewModel.kt` |
| State on rotation | `StateDemoCard`: remember resets, rememberSaveable and ViewModel survive |
