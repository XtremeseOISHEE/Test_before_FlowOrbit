package com.example.floworbit.presentation.task

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floworbit.domain.model.Priority // ⭐ 1. Import Priority
import com.example.floworbit.presentation.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(taskId: String, onBack: () -> Unit) {

    val vm: HomeViewModel = viewModel()
    val task = vm.tasks.collectAsState().value.firstOrNull { it.id == taskId }

    // State for all the editable fields
    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var category by remember(task) { mutableStateOf(task?.category ?: "") }
    // ⭐ 2. Add state for priority, converting the Int from the database to our Enum
    var priority by remember(task) { mutableStateOf(Priority.fromInt(task?.priority ?: 0)) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            // ⭐ 3. Add the Priority Section UI
            Spacer(modifier = Modifier.height(16.dp))
            Text("Priority", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { prio ->
                    FilterChip(
                        selected = priority == prio,
                        onClick = { priority = prio },
                        label = { Text(prio.name) }
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        task?.let {
                            vm.updateTask(
                                it.copy(
                                    title = title,
                                    description = description,
                                    category = if (category.isBlank()) null else category,
                                    // ⭐ 4. Save the selected priority's integer value
                                    priority = priority.value
                                )
                            )
                        }
                        onBack() // Go back after saving
                    }
                ) { Text("Save") }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        task?.let {
                            vm.deleteTask(it.id)
                            onBack()
                        }
                    }
                ) { Text("Delete") }
            }
        }
    }
}
