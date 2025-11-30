package com.example.floworbit.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics // ⭐ 1. Import Analytics Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel(),
    onOpenTask: (String) -> Unit,
    onStartFocus: () -> Unit,
    onOpenBlockedApps: () -> Unit,
    onOpenAnalytics: () -> Unit // ⭐ 2. Add the missing parameter
) {
    val tasks by vm.tasks.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FlowOrbit") },
                actions = {
                    // This was your existing button for Blocked Apps
                    IconButton(onClick = onOpenBlockedApps) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Blocked Apps"
                        )
                    }
                    // ⭐ 3. Add the new button for Analytics
                    IconButton(onClick = onOpenAnalytics) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Open Analytics"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) { // Aligned to end for consistency
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
                ExtendedFloatingActionButton(onClick = onStartFocus) {
                    Text("Focus")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

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
                    items(tasks, key = { it.id }) { task -> // Added key for performance
                        TaskItem(
                            task = task,
                            onClick = { onOpenTask(task.id) },
                            onDelete = { vm.deleteTask(task.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

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

// This is YOUR original TaskItem composable, with no checkbox.
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
            // I'm using the Delete Icon here as it's better than text,
            // but the functionality is identical to your original code.
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task")
            }
        }
    }
}
