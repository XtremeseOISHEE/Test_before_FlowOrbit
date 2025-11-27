package com.example.floworbit.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenTask: (String) -> Unit,
    onStartFocus: () -> Unit = {}
) {
    val vm: HomeViewModel = viewModel()
    val tasks by vm.tasks.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("FlowOrbit") })
        },

        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }

                ExtendedFloatingActionButton(
                    onClick = { onStartFocus() }
                ) {
                    Text("Focus")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            // Empty state
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks yet. Tap + to add one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = { onOpenTask(task.id) },
                            onDelete = { vm.deleteTask(task.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Dialog for adding a task
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add Task") },

                    text = {
                        Column {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") }
                            )
                        }
                    },

                    confirmButton = {
                        TextButton(onClick = {
                            vm.addTask(
                                title = title,
                                description = description,
                                category = category.takeIf { it.isNotBlank() }
                            )
                            title = ""
                            description = ""
                            category = ""
                            showDialog = false
                        }) {
                            Text("Add")
                        }
                    },

                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(task.title, style = MaterialTheme.typography.titleMedium)

                if (task.description.isNotEmpty()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall)
                }

                task.category?.let {
                    Text("Category: $it", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(onClick = onDelete) {
                Text("Del")
            }
        }
    }
}
