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
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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