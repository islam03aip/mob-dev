package com.example.libraryapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var selectedGenre by rememberSaveable { mutableStateOf("All") }
    var onlyAvailable by rememberSaveable { mutableStateOf(false) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var savedTaps by rememberSaveable { mutableStateOf(0) }

    var rememberTaps by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val genres = listOf("All") + state.books.map { it.genre }.toSet().sorted()
    val visibleBooks = filterBooks(state.books, query, selectedGenre, onlyAvailable)

    Scaffold(
        topBar = { TopAppBar(title = { Text("City Library") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SearchField(query = query, onQueryChange = { query = it })
            Spacer(Modifier.height(8.dp))
            GenreFilterRow(genres, selectedGenre) { selectedGenre = it }
            OnlyAvailableRow(onlyAvailable) { onlyAvailable = it }
            Text(
                "Borrowed: ${state.borrowedIds.size}/$MAX_LOANS",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                item {
                    StateDemoCard(
                        rememberTaps = rememberTaps,
                        savedTaps = savedTaps,
                        viewModelTaps = state.viewModelTaps,
                        onRememberTap = { rememberTaps++ },
                        onSavedTap = { savedTaps++ },
                        onViewModelTap = viewModel::tapViewModelCounter
                    )
                }
                items(visibleBooks, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        isBorrowed = book.id in state.borrowedIds,
                        enabled = !state.isBusy,
                        onBorrow = { viewModel.borrow(book.id) },
                        onReturn = { viewModel.returnBook(book.id) }
                    )
                }
                if (visibleBooks.isEmpty()) {
                    item { Text("No books found.", modifier = Modifier.padding(16.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, author, year, genre, copies ->
                viewModel.addBook(title, author, year, genre, copies)
                showAddDialog = false
            }
        )
    }
}


@Composable
fun BookCard(
    book: Book,
    isBorrowed: Boolean,
    enabled: Boolean,
    onBorrow: () -> Unit,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val available = book.available > 0
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (available) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = if (available) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = if (available) "Available" else "Not available",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium)
                Text("${book.author} • ${book.year}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${book.genre} • ${book.available}/${book.totalCopies} available",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (isBorrowed) {
                OutlinedButton(onClick = onReturn, enabled = enabled) { Text("Return") }
            } else {
                Button(onClick = onBorrow, enabled = enabled && available) { Text("Borrow") }
            }
        }
    }
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Search title or author") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilterRow(genres: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            FilterChip(
                selected = genre == selected,
                onClick = { onSelected(genre) },
                label = { Text(genre) }
            )
        }
    }
}

@Composable
fun OnlyAvailableRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text("Show only available books")
    }
}

@Composable
fun StateDemoCard(
    rememberTaps: Int,
    savedTaps: Int,
    viewModelTaps: Int,
    onRememberTap: () -> Unit,
    onSavedTap: () -> Unit,
    onViewModelTap: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("State demo – tap, then rotate the device", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRememberTap) { Text("remember: $rememberTaps") }
                OutlinedButton(onClick = onSavedTap) { Text("saveable: $savedTaps") }
                OutlinedButton(onClick = onViewModelTap) { Text("VM: $viewModelTaps") }
            }
        }
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, author: String, year: Int, genre: String, copies: Int) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var genre by rememberSaveable { mutableStateOf("") }
    var copies by rememberSaveable { mutableStateOf("1") }

    val valid = title.isNotBlank() && author.isNotBlank() && genre.isNotBlank() &&
        year.toIntOrNull() != null && (copies.toIntOrNull() ?: 0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(author, { author = it }, label = { Text("Author") }, singleLine = true)
                OutlinedTextField(
                    year, { year = it }, label = { Text("Year") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(genre, { genre = it }, label = { Text("Genre") }, singleLine = true)
                OutlinedTextField(
                    copies, { copies = it }, label = { Text("Copies") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onAdd(title.trim(), author.trim(), year.toInt(), genre.trim(), copies.toInt()) }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
