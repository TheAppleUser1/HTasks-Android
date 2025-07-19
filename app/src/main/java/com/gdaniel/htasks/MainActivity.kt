@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gdaniel.htasks

import android.content.Context
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.List
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.runBlocking

// Priority and Category enums for Android
enum class TaskPriority(val label: String, val color: Color) {
    Easy("Low", Color(0xFF4CAF50)),
    Medium("Medium", Color(0xFFFF9800)),
    Difficult("High", Color(0xFFF44336))
}

enum class TaskCategory(val label: String, val icon: String, val color: Color) {
    Personal("Personal", "person", Color(0xFF2196F3)),
    Work("Work", "work", Color(0xFFFF9800)),
    Shopping("Shopping", "shopping_cart", Color(0xFF4CAF50)),
    Health("Health", "favorite", Color(0xFFF44336)),
    Education("Education", "school", Color(0xFF9C27B0)),
    Social("Social", "group", Color(0xFFE91E63))
}

data class Task(
    val title: String,
    val priority: TaskPriority,
    val category: TaskCategory,
    val dueDate: String? = null,
    val isCompleted: Boolean = false
)

@Composable
fun WelcomeScreen() {
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var newTaskTitle by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.Easy) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.Personal) }
    val presetTasks = listOf("Wash the dishes", "Clean the Windows", "Mop the Floor", "Clean your room")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "Welcome to HTasks!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Get Motivated",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text("Type your own task") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Priority", modifier = Modifier.width(70.dp))
            Spacer(Modifier.width(8.dp))
            TaskPriority.values().forEach { priority ->
                val selected = selectedPriority == priority
                FilterChip(
                    selected = selected,
                    onClick = { selectedPriority = priority },
                    label = { Text(priority.label) },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priority.color.copy(alpha = 0.2f),
                        selectedLabelColor = priority.color,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Category", modifier = Modifier.width(70.dp))
            Spacer(Modifier.width(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedCategory.label)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    TaskCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add due date")
            Spacer(Modifier.width(8.dp))
            Switch(checked = showDatePicker, onCheckedChange = { showDatePicker = it })
        }
        if (showDatePicker) {
            // For simplicity, just use a text field for due date (could use a real date picker)
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (e.g. 2024-05-01)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (newTaskTitle.isNotBlank()) {
                    tasks = tasks + Task(
                        title = newTaskTitle,
                        priority = selectedPriority,
                        category = selectedCategory,
                        dueDate = if (showDatePicker && dueDate.isNotBlank()) dueDate else null
                    )
                    newTaskTitle = ""
                    dueDate = ""
                    showDatePicker = false
                    selectedPriority = TaskPriority.Easy
                    selectedCategory = TaskCategory.Personal
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = newTaskTitle.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Task")
        }
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Presets:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            presetTasks.forEach { preset ->
                OutlinedButton(
                    onClick = {
                        tasks = tasks + Task(
                            title = preset,
                            priority = TaskPriority.Easy,
                            category = TaskCategory.Personal
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(preset)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        if (tasks.isNotEmpty()) {
            Button(
                onClick = { /* TODO: Navigate to main app/home screen */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Continue", color = Color.White)
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Serializable
data class ChatMessage(val content: String, val isUser: Boolean, val timestamp: Long)

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
                text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (msg.isUser) Spacer(Modifier.weight(1f))
    }
}

// --- Achievements Data ---
enum class AchievementType(val title: String, val description: String, val icon: ImageVector, val showsProgress: Boolean = false, val progressTotal: Int = 1) {
    FirstTask("Getting Started", "Complete your first task", Icons.Filled.EmojiEvents),
    Streak3("On a Roll", "Maintain a 3-day streak", Icons.Filled.EmojiEvents, true, 3),
    Streak7("Consistency Master", "Maintain a 7-day streak", Icons.Filled.EmojiEvents, true, 7),
    TaskMaster("Task Master", "Complete 10 tasks", Icons.Filled.EmojiEvents, true, 10),
    WeekendWarrior("Weekend Warrior", "Complete 5 tasks in a week", Icons.Filled.EmojiEvents, true, 5),
    BalancedLife("Balanced Life", "Complete tasks in 3 different categories", Icons.Filled.EmojiEvents, true, 3)
}

data class Achievement(
    val type: AchievementType,
    val isUnlocked: Boolean,
    val progress: Int = 0
)

fun calculateAchievements(tasks: List<Task>, streaks: Streaks): List<Achievement> {
    val totalCompleted = tasks.count { it.isCompleted }
    val categoriesCompleted = tasks.filter { it.isCompleted }.map { it.category }.distinct().size
    val completedThisWeek = tasks.count { it.isCompleted && isInCurrentWeek(it) }
    return listOf(
        Achievement(AchievementType.FirstTask, totalCompleted >= 1),
        Achievement(AchievementType.Streak3, streaks.current >= 3, minOf(streaks.current, 3)),
        Achievement(AchievementType.Streak7, streaks.current >= 7, minOf(streaks.current, 7)),
        Achievement(AchievementType.TaskMaster, totalCompleted >= 10, minOf(totalCompleted, 10)),
        Achievement(AchievementType.WeekendWarrior, completedThisWeek >= 5, minOf(completedThisWeek, 5)),
        Achievement(AchievementType.BalancedLife, categoriesCompleted >= 3, minOf(categoriesCompleted, 3))
    )
}

// --- Achievements Sheet ---
@Composable
fun AchievementsSheet(tasks: List<Task>, streaks: Streaks, onDismiss: () -> Unit) {
    val achievements = calculateAchievements(tasks, streaks)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Achievements", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            achievements.forEach { achievement ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(achievement.type.icon, contentDescription = null, tint = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(achievement.type.title, fontWeight = FontWeight.Bold)
                            if (achievement.isUnlocked) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp).padding(start = 4.dp))
                            }
                        }
                        Text(achievement.type.description, fontSize = 14.sp, color = Color.Gray)
                        if (achievement.type.showsProgress && !achievement.isUnlocked) {
                            LinearProgressIndicator(progress = { achievement.progress / achievement.type.progressTotal.toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp))
                            Text("${achievement.progress}/${achievement.type.progressTotal}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

data class Settings(
    var showSocialFeatures: Boolean = false,
    var showDeleteConfirmation: Boolean = true,
    var deleteConfirmationText: String = "Are you sure you want to delete this task?",
    var notificationsEnabled: Boolean = true,
    var theme: String = "system" // "system", "light", "dark"
)

@Composable
fun SettingsSheet(settings: Settings, onSettingsChanged: (Settings) -> Unit, onDismiss: () -> Unit) {
    var localSettings by remember { mutableStateOf(settings.copy()) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Text("Customization", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show Social Features (BETA)", modifier = Modifier.weight(1f))
                Switch(checked = localSettings.showSocialFeatures, onCheckedChange = {
                    localSettings = localSettings.copy(showSocialFeatures = it)
                })
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show Confirmation when clicking delete", modifier = Modifier.weight(1f))
                Switch(checked = localSettings.showDeleteConfirmation, onCheckedChange = {
                    localSettings = localSettings.copy(showDeleteConfirmation = it)
                })
            }
            Spacer(Modifier.height(10.dp))
            Text("Change the Confirmation text when clicking delete")
            OutlinedTextField(
                value = localSettings.deleteConfirmationText,
                onValueChange = {
                    localSettings = localSettings.copy(deleteConfirmationText = it)
                },
                enabled = localSettings.showDeleteConfirmation,
                modifier = Modifier.fillMaxWidth().alpha(if (localSettings.showDeleteConfirmation) 1f else 0.4f)
            )
            Spacer(Modifier.height(20.dp))
            Text("Theme", fontWeight = FontWeight.Bold)
            Row {
                listOf("system", "light", "dark").forEach { theme ->
                    FilterChip(
                        selected = localSettings.theme == theme,
                        onClick = { localSettings = localSettings.copy(theme = theme) },
                        label = { Text(theme.replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notifications Enabled", modifier = Modifier.weight(1f))
                Switch(checked = localSettings.notificationsEnabled, onCheckedChange = {
                    localSettings = localSettings.copy(notificationsEnabled = it)
                })
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                onSettingsChanged(localSettings)
                onDismiss()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}

// --- Feed Data Model ---
data class FeedItem(
    val userId: String = "",
    val userName: String = "Anonymous",
    val taskTitle: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
)

// --- FeedSheet Composable ---
@Composable
fun FeedSheet(onDismiss: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    var feedItems by remember { mutableStateOf(listOf<Pair<String, FeedItem>>()) } // Pair<docId, FeedItem>
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    fun toggleLike(docId: String, item: FeedItem) {
        val liked = item.likedBy.contains(currentUserId)
        val newLikes = if (liked) item.likes - 1 else item.likes + 1
        val newLikedBy = if (liked) item.likedBy - currentUserId else item.likedBy + currentUserId
        db.collection("feed").document(docId)
            .update(mapOf("likes" to newLikes, "likedBy" to newLikedBy))
    }
    LaunchedEffect(Unit) {
        loading = true
        val snapshot = db.collection("feed").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(50).get().await()
        feedItems = snapshot.documents.mapNotNull { doc ->
            val item = doc.toObject(FeedItem::class.java)
            if (item != null) doc.id to item else null
        }
        loading = false
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Social Feed", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            if (loading) {
                CircularProgressIndicator()
            } else if (feedItems.isEmpty()) {
                Text("No feed items yet.", color = Color.Gray)
            } else {
                LazyColumn {
                    items(feedItems) { (docId, item) ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(item.userName, fontWeight = FontWeight.Bold)
                            Text(item.taskTitle)
                            Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.timestamp)), fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { scope.launch { toggleLike(docId, item) } }) {
                                    Icon(
                                        if (item.likedBy.contains(currentUserId)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = if (item.likedBy.contains(currentUserId)) "Unlike" else "Like",
                                        tint = if (item.likedBy.contains(currentUserId)) Color.Red else Color.Gray
                                    )
                                }
                                Text(item.likes.toString())
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

// --- Update HomeScreen to add Feed FAB and logic ---
@Composable
fun HomeScreen(
    tasks: List<Task>,
    onToggleComplete: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onAddTask: (Task) -> Unit,
    onEditTask: (Int, Task) -> Unit,
    settings: Settings,
    onSettingsChanged: (Settings) -> Unit,
    userId: String,
    userName: String,
    onFeedClick: () -> Unit,
    showFeed: Boolean,
    onDismissFeed: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var showStats by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val completedThisWeek = tasks.count { it.isCompleted && isInCurrentWeek(it) }
    val streaks = calculateStreaks(tasks)
    Scaffold(
        floatingActionButton = {
            Row {
                if (settings.showSocialFeatures) {
                    FloatingActionButton(onClick = onFeedClick, modifier = Modifier.padding(end = 12.dp)) {
                        Icon(Icons.Filled.Article, contentDescription = "Feed")
                    }
                }
                FloatingActionButton(onClick = { showStats = true }, modifier = Modifier.padding(end = 12.dp)) {
                    Icon(Icons.Filled.BarChart, contentDescription = "Statistics")
                }
                FloatingActionButton(onClick = { showAchievements = true }, modifier = Modifier.padding(end = 12.dp)) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements")
                }
                FloatingActionButton(onClick = { showSettings = true }, modifier = Modifier.padding(end = 12.dp)) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Number of tasks done this week:",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "$completedThisWeek",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                if (completedThisWeek == 0) {
                    Text(
                        "u lazy or sum?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks.size) { index ->
                    val task = tasks[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { editIndex = index },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Priority dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(task.priority.color, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            // Category label
                            Text(task.category.label, fontSize = 14.sp, color = task.category.color)
                            Spacer(Modifier.width(8.dp))
                            // Title and due date
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground
                                )
                                if (!task.dueDate.isNullOrBlank()) {
                                    Text(
                                        task.dueDate ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            // Motivation button (icon only, no action)
                            IconButton(onClick = { /* TODO: Motivation */ }) {
                                Icon(Icons.Filled.Star, contentDescription = "Motivation")
                            }
                            // Complete button (icon only, toggles completion)
                            IconButton(onClick = { onToggleComplete(index) }) {
                                Icon(
                                    if (task.isCompleted) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Complete"
                                )
                            }
                            // Delete button
                            IconButton(onClick = { onDelete(index) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
        if (showAddDialog) {
            AddTaskDialog(
                onAdd = { task -> onAddTask(task); showAddDialog = false },
                onDismiss = { showAddDialog = false }
            )
        }
        if (editIndex != null) {
            val task = tasks[editIndex!!]
            EditTaskDialog(
                initialTask = task,
                onEdit = { updated -> onEditTask(editIndex!!, updated); editIndex = null },
                onDismiss = { editIndex = null }
            )
        }
        if (showStats) {
            StatisticsSheet(tasks = tasks, onDismiss = { showStats = false })
        }
        if (showAchievements) {
            AchievementsSheet(tasks = tasks, streaks = streaks, onDismiss = { showAchievements = false })
        }
        if (showSettings) {
            SettingsSheet(settings = settings, onSettingsChanged = onSettingsChanged, onDismiss = { showSettings = false })
        }
        if (showFeed) {
            FeedSheet(onDismiss = onDismissFeed)
        }
    }
}

fun isInCurrentWeek(task: Task): Boolean {
    // Simple week check: tasks with dueDate in this week or completed this week
    // For demo, just count all completed tasks (customize as needed)
    return true
}

@Composable
fun StatisticsSheet(tasks: List<Task>, onDismiss: () -> Unit) {
    val timeRanges = listOf("Week", "Month", "Year", "All Time")
    var selectedRange by remember { mutableStateOf(0) }
    val now = remember { java.util.Calendar.getInstance() }
    val filteredTasks = remember(tasks, selectedRange) {
        // Filter tasks by selected time range
        val cal = java.util.Calendar.getInstance()
        tasks.filter { task ->
            if (!task.isCompleted) return@filter false
            val dueDate = task.dueDate?.let {
                try {
                    SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(it)
                } catch (e: Exception) { null }
            } ?: return@filter true
            when (selectedRange) {
                0 -> { // Week
                    val week = cal.get(java.util.Calendar.WEEK_OF_YEAR)
                    val year = cal.get(java.util.Calendar.YEAR)
                    val taskCal = java.util.Calendar.getInstance().apply { time = dueDate }
                    week == taskCal.get(java.util.Calendar.WEEK_OF_YEAR) && year == taskCal.get(java.util.Calendar.YEAR)
                }
                1 -> { // Month
                    val month = cal.get(java.util.Calendar.MONTH)
                    val year = cal.get(java.util.Calendar.YEAR)
                    val taskCal = java.util.Calendar.getInstance().apply { time = dueDate }
                    month == taskCal.get(java.util.Calendar.MONTH) && year == taskCal.get(java.util.Calendar.YEAR)
                }
                2 -> { // Year
                    val year = cal.get(java.util.Calendar.YEAR)
                    val taskCal = java.util.Calendar.getInstance().apply { time = dueDate }
                    year == taskCal.get(java.util.Calendar.YEAR)
                }
                else -> true
            }
        }
    }
    val totalCompleted = filteredTasks.size
    val todayCompleted = filteredTasks.count {
        val cal = java.util.Calendar.getInstance()
        val dueDate = it.dueDate?.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(it)
            } catch (e: Exception) { null }
        } ?: return@count false
        val today = cal.get(java.util.Calendar.DAY_OF_YEAR)
        val year = cal.get(java.util.Calendar.YEAR)
        val taskCal = java.util.Calendar.getInstance().apply { time = dueDate }
        today == taskCal.get(java.util.Calendar.DAY_OF_YEAR) && year == taskCal.get(java.util.Calendar.YEAR)
    }
    val categoryCounts = TaskCategory.values().associateWith { cat -> filteredTasks.count { it.category == cat } }
    val priorityCounts = TaskPriority.values().associateWith { prio -> filteredTasks.count { it.priority == prio } }
    // Streak calculation (simple: consecutive days with completed tasks)
    val streaks = calculateStreaks(tasks)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Statistics", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                timeRanges.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedRange == idx,
                        onClick = { selectedRange = idx },
                        label = { Text(label) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Streak Stats", fontWeight = FontWeight.Bold)
            Row {
                StatCard(title = "Current Streak", value = streaks.current.toString())
                Spacer(Modifier.width(16.dp))
                StatCard(title = "Longest Streak", value = streaks.longest.toString())
            }
            Spacer(Modifier.height(16.dp))
            Text("Task Completion", fontWeight = FontWeight.Bold)
            Row {
                StatCard(title = "Total Tasks", value = totalCompleted.toString())
                Spacer(Modifier.width(16.dp))
                StatCard(title = "Today", value = todayCompleted.toString())
            }
            Spacer(Modifier.height(16.dp))
            Text("Category Distribution", fontWeight = FontWeight.Bold)
            categoryCounts.forEach { (cat, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cat.label, color = cat.color, modifier = Modifier.width(100.dp))
                    LinearProgressIndicator(progress = { if (totalCompleted > 0) count / totalCompleted.toFloat() else 0f }, modifier = Modifier.weight(1f).height(8.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(count.toString())
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Priority Distribution", fontWeight = FontWeight.Bold)
            priorityCounts.forEach { (prio, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(prio.label, color = prio.color, modifier = Modifier.width(100.dp))
                    LinearProgressIndicator(progress = { if (totalCompleted > 0) count / totalCompleted.toFloat() else 0f }, modifier = Modifier.weight(1f).height(8.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(count.toString())
                }
            }
        }
    }
}

data class Streaks(val current: Int, val longest: Int)
fun calculateStreaks(tasks: List<Task>): Streaks {
    // Simple streak calculation: count consecutive days with completed tasks
    val completedDates = tasks.filter { it.isCompleted && it.dueDate != null }.mapNotNull {
        try {
            SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(it.dueDate)
        } catch (e: Exception) { null }
    }.sorted()
    if (completedDates.isEmpty()) return Streaks(0, 0)
    var currentStreak = 1
    var longestStreak = 1
    for (i in 1 until completedDates.size) {
        val prev = completedDates[i - 1]
        val curr = completedDates[i]
        val diff = ((curr.time - prev.time) / (1000 * 60 * 60 * 24)).toInt()
        if (diff == 1) {
            currentStreak++
            if (currentStreak > longestStreak) longestStreak = currentStreak
        } else {
            currentStreak = 1
        }
    }
    return Streaks(currentStreak, longestStreak)
}

@Composable
fun StatCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddTaskDialog(onAdd: (Task) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.Easy) }
    var category by remember { mutableStateOf(TaskCategory.Personal) }
    var dueDate by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task name") })
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Priority", modifier = Modifier.width(70.dp))
                    TaskPriority.values().forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Category", modifier = Modifier.width(70.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { expanded = true }) {
                            Text(category.label)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            TaskCategory.values().forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.label) },
                                    onClick = {
                                        category = c
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (optional)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onAdd(Task(title, priority, category, if (dueDate.isNotBlank()) dueDate else null, false))
                }
            }) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditTaskDialog(initialTask: Task, onEdit: (Task) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(initialTask.title) }
    var priority by remember { mutableStateOf(initialTask.priority) }
    var category by remember { mutableStateOf(initialTask.category) }
    var dueDate by remember { mutableStateOf(initialTask.dueDate ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task name") })
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Priority", modifier = Modifier.width(70.dp))
                    TaskPriority.values().forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Category", modifier = Modifier.width(70.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { expanded = true }) {
                            Text(category.label)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            TaskCategory.values().forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.label) },
                                    onClick = {
                                        category = c
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (optional)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onEdit(initialTask.copy(title = title, priority = priority, category = category, dueDate = if (dueDate.isNotBlank()) dueDate else null))
                }
            }) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- Username Dialog ---
@Composable
fun UsernameDialog(onSubmit: (String) -> Unit, error: String?) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Choose a username") },
        text = {
            Column {
                Text("Enter the name you want to appear with in the feed.")
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
                if (!error.isNullOrEmpty()) {
                    Text(error, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(username) }, enabled = username.isNotBlank()) { Text("Save") }
        }
    )
}

// --- Slur/Inappropriate Word Filter ---
val blockedWords = listOf(
    "fuck", "shit", "bitch", "nigger", "faggot", "cunt", "retard", "asshole", "whore", "dick", "pussy", "bastard", "slut", "hitler", "adolf", "gay", "trans", "furry", "pride", "fucker", "ass"
)
fun containsSlur(username: String): Boolean {
    val lower = username.lowercase()
    return blockedWords.any { lower.contains(it) }
}

// --- Update MainActivity for Username at Signup ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HTasksTheme {
                var showWelcome by remember { mutableStateOf(true) }
                var tasks by remember { mutableStateOf(listOf<Task>()) }
                var settings by remember { mutableStateOf(Settings()) }
                val db = FirebaseFirestore.getInstance()
                var userId by remember { mutableStateOf("") }
                var localUserName by remember { mutableStateOf("") }
                var showFeed by remember { mutableStateOf(false) }
                var showUsernameDialog by remember { mutableStateOf(false) }
                var dialogError by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser ?: auth.signInAnonymously().await().user
                    userId = user?.uid ?: ""
                    localUserName = user?.displayName ?: ""
                    loading = false
                }
                fun uploadFeedItem(task: Task) {
                    if (settings.showSocialFeatures) {
                        val feedItem = FeedItem(
                            userId = userId,
                            userName = localUserName,
                            taskTitle = task.title,
                            timestamp = System.currentTimeMillis()
                        )
                        db.collection("feed").add(feedItem)
                    }
                }
                if (loading || userId.isBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (showUsernameDialog) {
                    UsernameDialog(
                        onSubmit = { name ->
                            if (containsSlur(name)) {
                                dialogError = "Please enter a username without slurs or inappropriate words."
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val auth = FirebaseAuth.getInstance()
                                    val user = auth.currentUser
                                    user?.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
                                }
                                localUserName = name
                                showUsernameDialog = false
                                showFeed = true
                            }
                        },
                        error = dialogError
                    )
                } else if (showWelcome) {
                    WelcomeScreen(
                        tasks = tasks,
                        onTasksChanged = { tasks = it },
                        onContinue = { showWelcome = false }
                    )
                } else {
                    HomeScreen(
                        tasks = tasks,
                        onToggleComplete = { idx ->
                            tasks = tasks.toMutableList().also {
                                val t = it[idx]
                                val updated = t.copy(isCompleted = !t.isCompleted)
                                it[idx] = updated
                                if (updated.isCompleted) uploadFeedItem(updated)
                            }
                        },
                        onDelete = { idx -> tasks = tasks.toMutableList().also { it.removeAt(idx) } },
                        onAddTask = { task ->
                            tasks = tasks + task
                            if (task.isCompleted) uploadFeedItem(task)
                        },
                        onEditTask = { idx, updated ->
                            tasks = tasks.toMutableList().also {
                                it[idx] = updated
                                if (updated.isCompleted) uploadFeedItem(updated)
                            }
                        },
                        settings = settings,
                        onSettingsChanged = { settings = it },
                        userId = userId,
                        userName = localUserName,
                        onFeedClick = {
                            if (localUserName.isBlank()) {
                                showUsernameDialog = true
                            } else {
                                showFeed = true
                            }
                        },
                        showFeed = showFeed,
                        onDismissFeed = { showFeed = false }
                    )
                }
            }
        }
    }
}

// Update WelcomeScreen to accept tasks, onTasksChanged, and onContinue
@Composable
fun WelcomeScreen(
    tasks: List<Task>,
    onTasksChanged: (List<Task>) -> Unit,
    onContinue: () -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.Easy) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.Personal) }
    val presetTasks = listOf("Wash the dishes", "Clean the Windows", "Mop the Floor", "Clean your room")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "Welcome to HTasks!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Get Motivated",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text("Type your own task") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Priority", modifier = Modifier.width(70.dp))
            Spacer(Modifier.width(8.dp))
            TaskPriority.values().forEach { priority ->
                val selected = selectedPriority == priority
                FilterChip(
                    selected = selected,
                    onClick = { selectedPriority = priority },
                    label = { Text(priority.label) },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priority.color.copy(alpha = 0.2f),
                        selectedLabelColor = priority.color,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Category", modifier = Modifier.width(70.dp))
            Spacer(Modifier.width(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedCategory.label)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    TaskCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add due date")
            Spacer(Modifier.width(8.dp))
            Switch(checked = showDatePicker, onCheckedChange = { showDatePicker = it })
        }
        if (showDatePicker) {
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (e.g. 2024-05-01)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (newTaskTitle.isNotBlank()) {
                    onTasksChanged(tasks + Task(
                        title = newTaskTitle,
                        priority = selectedPriority,
                        category = selectedCategory,
                        dueDate = if (showDatePicker && dueDate.isNotBlank()) dueDate else null
                    ))
                    newTaskTitle = ""
                    dueDate = ""
                    showDatePicker = false
                    selectedPriority = TaskPriority.Easy
                    selectedCategory = TaskCategory.Personal
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = newTaskTitle.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Task")
        }
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Presets:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            presetTasks.forEach { preset ->
                OutlinedButton(
                    onClick = {
                        onTasksChanged(tasks + Task(
                            title = preset,
                            priority = TaskPriority.Easy,
                            category = TaskCategory.Personal
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(preset)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        if (tasks.isNotEmpty()) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Continue", color = Color.White)
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}