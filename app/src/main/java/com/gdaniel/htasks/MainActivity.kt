a
AchievementsDialog
achievements
AchievementsDialoga
a
achievements
a
a
a
a
a
a
a
a
a
a
onAdd
a
achievements
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
AchievementsDialoga
a
a
achievementsa
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
achievementsa
a
a
a
a
a
a
AchievementsDialog
aa
a
AddTaskDialoga
a
a
aaa
aaa
AddTaskDialog
aaa
AddTaskDialoga
AchievementsDialogaa
aa
aaa
a
aa
aa
aűű
a
a
a
a
AddTaskDialogaa
aa
aaa
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
a
aaa
a
aaa
aaa
aaa
a
a
a
a
a
a
aaa
a
a
a
a
a
a
a
a
aaa
a
a
a
a
a
aaa
a
a
a
a
a
a
a
a
aaa
aaa
a
a
a
a
a
a
a
a
a
a
a
a
a
aaa

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gdaniel.htasks

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.gdaniel.htasks.ui.theme.HTasksTheme
import kotlinx.coroutines.launch
import java.util.*
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import com.google.firebase.FirebaseApp

// --- Data Models ---
@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val category: TaskCategory = TaskCategory.Personal,
    val priority: TaskPriority = TaskPriority.Medium
)

@Serializable
enum class TaskPriority(val label: String, val color: Color) {
    Low("Low", Color(0xFF4CAF50)),
    Medium("Medium", Color(0xFFFF9800)),
    High("High", Color(0xFFF44336))
}

@Serializable
enum class TaskCategory(val label: String, val icon: String, val color: Color) {
    Personal("Personal", "\uD83D\uDC64", Color(0xFF2196F3)),
    Work("Work", "\uD83D\uDCBC", Color(0xFFFF9800)),
    Shopping("Shopping", "\uD83D\uDED2", Color(0xFF4CAF50)),
    Health("Health", "\u2764\uFE0F", Color(0xFFF44336)),
    Education("Education", "\uD83D\uDCDA", Color(0xFF9C27B0)),
    Social("Social", "\uD83D\uDC65", Color(0xFFE91E63))
}

@Serializable
data class UserSettings(
    val showDeleteConfirmation: Boolean = true,
    val deleteConfirmationText: String = "Are you sure you want to delete this task?",
    val showSocialFeatures: Boolean = false
)

@Serializable
data class ChatMessage(val content: String, val isUser: Boolean, val timestamp: Long)

@Serializable
data class FeedItem(
    val id: String = "",
    val user: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

// --- Main Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HTasksTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val (tasks, setTasks) = rememberTasksState(context)
                var showWelcome by remember { mutableStateOf(!hasSeenWelcome(context)) }
                var showAchievements by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                var showStats by remember { mutableStateOf(false) }
                var showChat by remember { mutableStateOf(false) }
                var showFeed by remember { mutableStateOf(false) }
                var settings by remember { mutableStateOf(loadUserSettings(context)) }
                NavHost(
                    navController = navController,
                    startDestination = if (showWelcome) "welcome" else "home"
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onContinue = {
                                showWelcome = false
                                setHasSeenWelcome(context)
                                navController.navigate("home") { popUpTo("welcome") { inclusive = true } }
                            },
                            onAddTask = { task ->
                                setTasks(tasks + task)
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            tasks = tasks,
                            onAddTask = { setTasks(tasks + it) },
                            onToggleComplete = { task ->
                                setTasks(tasks.map { if (it.id == task.id) it.copy(isCompleted = !it.isCompleted) else it })
                            },
                            onDeleteTask = { task ->
                                setTasks(tasks.filter { it.id != task.id })
                            },
                            onShowAchievements = { showAchievements = true },
                            onShowSettings = { showSettings = true },
                            onShowStats = { showStats = true },
                            onShowChat = { showChat = true },
                            onShowFeed = { showFeed = true },
                            settings = settings
                        )
                    }
                }
                if (showAchievements) {
                    AchievementsDialog(onDismiss = { showAchievements = false }, tasks = tasks)
                }
                if (showSettings) {
                    SettingsDialog(
                        settings = settings,
                        onSettingsChange = {
                            settings = it
                            saveUserSettings(context, it)
                        },
                        onDismiss = { showSettings = false }
                    )
                }
                if (showStats) {
                    StatsDialog(onDismiss = { showStats = false })
                }
                if (showChat) {
                    ChatDialog(onDismiss = { showChat = false })
                }
                if (showFeed) {
                    FeedDialog(onDismiss = { showFeed = false })
                }
            }
        }
    }
}

// --- State Management ---
@Composable
fun rememberTasksState(context: Context): Pair<List<Task>, (List<Task>) -> Unit> {
    val prefs = context.getSharedPreferences("tasks", Context.MODE_PRIVATE)
    var tasks by remember { mutableStateOf(loadTasks(prefs)) }
    val setTasks: (List<Task>) -> Unit = {
        tasks = it
        saveTasks(prefs, it)
    }
    return tasks to setTasks
}

fun loadTasks(prefs: android.content.SharedPreferences): List<Task> {
    val json = prefs.getString("tasks", null) ?: return emptyList()
    return try {
        kotlinx.serialization.json.Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(Task.serializer()), json
        )
    } catch (e: Exception) { emptyList() }
}

fun saveTasks(prefs: android.content.SharedPreferences, tasks: List<Task>) {
    val json = kotlinx.serialization.json.Json.encodeToString(
        kotlinx.serialization.builtins.ListSerializer(Task.serializer()), tasks
    )
    prefs.edit { putString("tasks", json) }
}

fun hasSeenWelcome(context: Context): Boolean =
    context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("hasSeenWelcome", false)

fun setHasSeenWelcome(context: Context) =
    context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit { putBoolean("hasSeenWelcome", true) }

fun loadUserSettings(context: Context): UserSettings {
    val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    return UserSettings(
        showDeleteConfirmation = prefs.getBoolean("showDeleteConfirmation", true),
        deleteConfirmationText = prefs.getString("deleteConfirmationText", "Are you sure you want to delete this task?") ?: "Are you sure you want to delete this task?",
        showSocialFeatures = prefs.getBoolean("showSocialFeatures", false)
    )
}

fun saveUserSettings(context: Context, settings: UserSettings) {
    val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    prefs.edit {
        putBoolean("showDeleteConfirmation", settings.showDeleteConfirmation)
        putString("deleteConfirmationText", settings.deleteConfirmationText)
        putBoolean("showSocialFeatures", settings.showSocialFeatures)
    }
}

// --- Screens ---
@Composable
fun WelcomeScreen(onContinue: () -> Unit, onAddTask: (Task) -> Unit) {
    var title by remember { mutableStateOf("") }
    var showDate by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.Low) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.Personal) }
    val context = LocalContext.current
    val presetTasks = listOf("Wash the dishes", "Clean the Windows", "Mop the Floor", "Clean your room")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to HTasks!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Get Motivated", fontSize = 18.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Type your own task") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Priority:")
            Spacer(Modifier.width(8.dp))
            DropdownMenuBox(
                options = TaskPriority.entries.toList(),
                selected = selectedPriority,
                onSelected = { selectedPriority = it },
                label = { it.label }
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Category:")
            Spacer(Modifier.width(8.dp))
            DropdownMenuBox(
                options = TaskCategory.entries.toList(),
                selected = selectedCategory,
                onSelected = { selectedCategory = it },
                label = { it.label }
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showDate, onCheckedChange = { showDate = it })
            Text("Add due date")
        }
        if (showDate) {
            val dateStr = dueDate?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
            } ?: "Pick date"
            Button(onClick = {
                val now = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        val cal = Calendar.getInstance()
                        cal.set(y, m, d, 0, 0, 0)
                        dueDate = cal.timeInMillis
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) { Text(dateStr) }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (title.isNotBlank()) {
                onAddTask(
                    Task(
                        title = title,
                        dueDate = if (showDate) dueDate else null,
                        category = selectedCategory,
                        priority = selectedPriority
                    )
                )
                setHasSeenWelcome(context)
                title = ""
                showDate = false
                dueDate = null
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Task")
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth()) {
            Text("Presets:", fontWeight = FontWeight.Bold)
            presetTasks.forEach { preset ->
                Button(
                    onClick = {
                        onAddTask(Task(title = preset))
                        setHasSeenWelcome(context)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(preset)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            setHasSeenWelcome(context)
            onContinue()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}

@Composable
fun HomeScreen(
    tasks: List<Task>,
    onAddTask: (Task) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onShowAchievements: () -> Unit,
    onShowSettings: () -> Unit,
    onShowStats: () -> Unit,
    onShowChat: () -> Unit,
    onShowFeed: () -> Unit,
    settings: UserSettings
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                actions = {
                    IconButton(onClick = onShowAchievements) {
                        Icon(Icons.Default.Star, contentDescription = "Achievements")
                    }
                    IconButton(onClick = onShowSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(onClick = onShowStats, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Stats")
                }
                FloatingActionButton(onClick = onShowChat, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Chat")
                }
                if (settings.showSocialFeatures) {
                    FloatingActionButton(onClick = onShowFeed, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.List, contentDescription = "Feed")
                    }
                }
                FloatingActionButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks yet! Add one to get started.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(tasks) { task ->
                        TaskRow(
                            task = task,
                            onToggleComplete = { onToggleComplete(task) },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }
        if (showAddSheet) {
            AddTaskDialog(
                onAdd = {
                    onAddTask(it)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false }
            )
        }
        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Delete Task") },
                text = { Text(settings.deleteConfirmationText) },
                confirmButton = {
                    Button(onClick = {
                        onDeleteTask(taskToDelete!!)
                        taskToDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    Button(onClick = { taskToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun TaskRow(task: Task, onToggleComplete: () -> Unit, onDelete: () -> Unit) {
    val category = task.category
    val priority = task.priority
    val dueDateStr = task.dueDate?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(priority.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(priority.label.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(category.icon, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
    Text(
                    task.title,
                    fontWeight = if (task.isCompleted) FontWeight.Light else FontWeight.Bold,
                    color = if (task.isCompleted) Color.Gray else Color.Unspecified,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                if (dueDateStr != null) {
                    Text("Due: $dueDateStr", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleComplete() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AddTaskDialog(onAdd: (Task) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var showDate by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.Low) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.Personal) }
    val datePickerDialog = remember {
        mutableStateOf<DatePickerDialog?>(null)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Priority:")
                    Spacer(Modifier.width(8.dp))
                    DropdownMenuBox(
                        options = TaskPriority.entries.toList(),
                        selected = selectedPriority,
                        onSelected = { selectedPriority = it },
                        label = { it.label }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Category:")
                    Spacer(Modifier.width(8.dp))
                    DropdownMenuBox(
                        options = TaskCategory.entries.toList(),
                        selected = selectedCategory,
                        onSelected = { selectedCategory = it },
                        label = { it.label }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showDate, onCheckedChange = { showDate = it })
                    Text("Add due date")
                }
                if (showDate) {
                    val dateStr = dueDate?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    } ?: "Pick date"
                    Button(onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance()
                                cal.set(y, m, d, 0, 0, 0)
                                dueDate = cal.timeInMillis
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) { Text(dateStr) }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onAdd(
                        Task(
                            title = title,
                            dueDate = if (showDate) dueDate else null,
                            category = selectedCategory,
                            priority = selectedPriority
                        )
                    )
                    title = ""
                    showDate = false
                    dueDate = null
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun <T> DropdownMenuBox(options: List<T>, selected: T, onSelected: (T) -> Unit, label: (T) -> String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(label(selected))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Dialogs (stubs for now) ---
@Composable
fun AchievementsDialog(onDismiss: () -> Unit, tasks: List<Task>) {
    val completed = tasks.filter { it.isCompleted }
    val totalCompleted = completed.size
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val completedToday = completed.count {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.dueDate ?: 0)) == today
    }
    // Streak calculation
    val dates = completed.mapNotNull { it.dueDate }
        .map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) }
        .toSet()
    var streak = 0
    var maxStreak = 0
    var prev = today
    val cal = Calendar.getInstance()
    while (dates.contains(prev)) {
        streak++
        maxStreak = maxOf(maxStreak, streak)
        cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(prev)!!
        cal.add(Calendar.DAY_OF_YEAR, -1)
        prev = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
    val categories = completed.map { it.category }.toSet().size
    val week = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
    val weekStart = week.timeInMillis
    val completedThisWeek = completed.count { (it.dueDate ?: 0) >= weekStart }
    val achievements: List<Triple<String, String, Pair<Boolean, Pair<Int, Int>?>>> = listOf(
        Triple("Getting Started", "Complete your first task", Pair(totalCompleted >= 1, null)),
        Triple("On a Roll", "Maintain a 3-day streak", Pair(streak >= 3, Pair(streak, 3))),
        Triple("Consistency Master", "Maintain a 7-day streak", Pair(streak >= 7, Pair(streak, 7))),
        Triple("Task Master", "Complete 10 tasks", Pair(totalCompleted >= 10, Pair(totalCompleted, 10))),
        Triple("Weekend Warrior", "Complete 5 tasks in a week", Pair(completedThisWeek >= 5, Pair(completedThisWeek, 5))),
        Triple("Balanced Life", "Complete tasks in 3 different categories", Pair(categories >= 3, Pair(categories, 3)))
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Achievements") },
        text = {
            Column(Modifier.heightIn(max = 400.dp)) {
                achievements.forEachIndexed { i, (title, desc, status) ->
                    val (unlocked, progress) = status
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Icon(
                            if (unlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (unlocked) Color(0xFFFFD600) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold)
                            Text(desc, fontSize = 13.sp, color = Color.Gray)
                            if (progress != null && !unlocked) {
                                val cur = progress.first
                                val total = progress.second
                                LinearProgressIndicator(progress = cur / total.toFloat(), modifier = Modifier.fillMaxWidth().height(6.dp))
                                Text("$cur/$total", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        if (unlocked) {
                            Text("Unlocked!", color = Color(0xFF43A047), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Divider()
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun SettingsDialog(settings: UserSettings, onSettingsChange: (UserSettings) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(Modifier.heightIn(max = 300.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = settings.showDeleteConfirmation,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(showDeleteConfirmation = it))
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Show delete confirmation")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = settings.deleteConfirmationText,
                    onValueChange = {
                        onSettingsChange(settings.copy(deleteConfirmationText = it))
                    },
                    label = { Text("Delete confirmation text") },
                    enabled = settings.showDeleteConfirmation,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = settings.showSocialFeatures,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(showSocialFeatures = it))
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Show social features")
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
fun StatsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val (tasks, _) = rememberTasksState(context)
    val completed = tasks.filter { it.isCompleted }
    val totalCompleted = completed.size
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val completedToday = completed.count {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.dueDate ?: 0)) == today
    }
    // Streak calculation
    val dates = completed.mapNotNull { it.dueDate }
        .map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) }
        .toSet()
    var streak = 0
    var maxStreak = 0
    var prev = today
    val cal = Calendar.getInstance()
    while (dates.contains(prev)) {
        streak++
        maxStreak = maxOf(maxStreak, streak)
        cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(prev)!!
        cal.add(Calendar.DAY_OF_YEAR, -1)
        prev = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
    val categoryCounts = completed.groupingBy { it.category }.eachCount()
    val priorityCounts = completed.groupingBy { it.priority }.eachCount()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Statistics") },
        text = {
            Column(Modifier.heightIn(max = 400.dp)) {
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Streak Stats", fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Streak: $streak")
                            Text("Longest Streak: $maxStreak")
                        }
                    }
                }
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Task Completion", fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total: $totalCompleted")
                            Text("Today: $completedToday")
                        }
                    }
                }
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Category Distribution", fontWeight = FontWeight.Bold)
                        TaskCategory.entries.forEach { cat ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row { Text(cat.icon, fontSize = 16.sp); Spacer(Modifier.width(4.dp)); Text(cat.label) }
                                Text("${categoryCounts[cat] ?: 0}")
                            }
                        }
                    }
                }
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Priority Distribution", fontWeight = FontWeight.Bold)
                        TaskPriority.entries.forEach { prio ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row { Box(Modifier.size(12.dp).background(prio.color, CircleShape)); Spacer(Modifier.width(4.dp)); Text(prio.label) }
                                Text("${priorityCounts[prio] ?: 0}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun ChatDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
    var messages by remember { mutableStateOf(loadChatMessages(prefs)) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val remainingPrompts = PromptManager.remainingPrompts(context)
    val canSend = PromptManager.canSendPrompt(context) && inputText.isNotBlank() && !isLoading

    AlertDialog(
        onDismissRequest = {
            saveChatMessages(prefs, messages)
            onDismiss()
        },
        title = { Text("HTasksAI Chat") },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                    if (isLoading) {
                        item { LoadingBubble() }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Type your message...") },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (!canSend) return@Button
                        val userMsg = ChatMessage(inputText, true, System.currentTimeMillis())
                        messages = messages + userMsg
                        inputText = ""
                        isLoading = true
                        PromptManager.usePrompt(context)
                        scope.launch {
                            try {
                                val aiText = GeminiService.sendMessage(userMsg.content)
                                messages = messages + ChatMessage(aiText, false, System.currentTimeMillis())
                            } catch (e: Exception) {
                                messages = messages + ChatMessage("Sorry, I couldn't process your request.", false, System.currentTimeMillis())
                            } finally {
                                isLoading = false
                            }
                        }
                    }, enabled = canSend) {
                        Text("Send")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Prompts left: ${PromptManager.remainingPrompts(context)}", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = {
                saveChatMessages(prefs, messages)
                onDismiss()
            }) { Text("Close") }
        }
    )
}

@Composable
fun FeedDialog(onDismiss: () -> Unit) {
    val feed = remember { mutableStateListOf<FeedItem>() }
    var loading by remember { mutableStateOf(true) }
    var showPostField by remember { mutableStateOf(false) }
    var postText by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var usernameSet by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        db.collection("feed")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                feed.clear()
                snapshot?.documents?.forEach { doc ->
                    val item = FeedItem(
                        id = doc.id,
                        user = doc.getString("user") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                    feed.add(item)
                }
                loading = false
            }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Feed (Social Features)") },
        text = {
            Column {
                if (!usernameSet) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Enter a username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { if (username.isNotBlank()) usernameSet = true },
                        enabled = username.isNotBlank()
                    ) { Text("Set Username") }
                    Spacer(Modifier.height(16.dp))
                }
                if (loading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (feed.isEmpty()) {
                    Text("No feed items yet.")
                } else {
                    LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(feed) { item ->
                            FeedItemRow(item)
                            Divider()
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (showPostField && usernameSet) {
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        label = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Button(
                            onClick = {
                                if (postText.isNotBlank()) {
                                    val post = hashMapOf(
                                        "user" to username,
                                        "message" to postText,
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    db.collection("feed").add(post)
                                    postText = ""
                                    showPostField = false
                                }
                            },
                            enabled = postText.isNotBlank()
                        ) { Text("Post") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { showPostField = false }) { Text("Cancel") }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                FloatingActionButton(
                    onClick = { if (usernameSet) showPostField = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Post")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

@Composable
fun FeedItemRow(item: FeedItem) {
    Row(Modifier.padding(vertical = 8.dp)) {
        Column(Modifier.weight(1f)) {
            Text(item.user, fontWeight = FontWeight.Bold)
            Text(item.message)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        if (!msg.isUser) Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start) {
            Surface(
                color = if (msg.isUser) Color(0xFF1976D2) else Color(0xFFE3F2FD),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    msg.content,
                    color = if (msg.isUser) Color.White else Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (msg.isUser) Spacer(Modifier.weight(1f))
    }
}

@Composable
fun LoadingBubble() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator(Modifier.size(24.dp))
    }
}

fun loadChatMessages(prefs: android.content.SharedPreferences): List<ChatMessage> {
    val json = prefs.getString("messages", null) ?: return emptyList()
    return try {
        val arr = org.json.JSONArray(json)
        val result = mutableListOf<ChatMessage>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result.add(
                ChatMessage(
                    o.getString("content"),
                    o.getBoolean("isUser"),
                    o.getLong("timestamp")
                )
            )
        }
        result
    } catch (e: Exception) { emptyList() }
}

fun saveChatMessages(prefs: android.content.SharedPreferences, messages: List<ChatMessage>) {
    val arr = org.json.JSONArray()
    messages.forEach {
        val o = org.json.JSONObject()
        o.put("content", it.content)
        o.put("isUser", it.isUser)
        o.put("timestamp", it.timestamp)
        arr.put(o)
    }
    prefs.edit { putString("messages", arr.toString()) }
}