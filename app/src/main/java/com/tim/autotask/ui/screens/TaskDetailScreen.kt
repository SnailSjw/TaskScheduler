package com.tim.autotask.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tim.autotask.data.model.RepeatType
import com.tim.autotask.data.model.Task
import com.tim.autotask.data.model.TaskLog
import com.tim.autotask.data.model.TaskStatus
import com.tim.autotask.data.model.TaskType
import com.tim.autotask.ui.components.TimelineView
import com.tim.autotask.ui.theme.TaskCompleted
import com.tim.autotask.ui.theme.TaskExecuting
import com.tim.autotask.ui.theme.TaskFailed
import com.tim.autotask.ui.theme.TaskPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel()
) {
    val task by viewModel.observeTask(taskId).collectAsState()
    val logs by viewModel.observeLogs(taskId).collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && task != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除任务「${task!!.name}」吗？所有执行日志也将被清除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task!!) {
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (task == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val t = task!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (t.type) {
                                TaskType.SCHEDULED_LAUNCH -> Icons.Default.Timer
                                TaskType.LOCATION_TRIGGERED -> Icons.Default.LocationOn
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        StatusTag(status = t.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("类型", when (t.type) {
                        TaskType.SCHEDULED_LAUNCH -> "定时启动 App"
                        TaskType.LOCATION_TRIGGERED -> "定位触发"
                    })
                    InfoRow("目标应用", t.targetAppLabel)
                    InfoRow("执行时间", formatDateTime(t.scheduledTime))
                    InfoRow("重复", when (t.repeatType) {
                        RepeatType.NONE -> "不重复"
                        RepeatType.DAILY -> "每天"
                        RepeatType.WEEKLY -> "每周"
                    })

                    if (t.type == TaskType.LOCATION_TRIGGERED && t.targetAddress != null) {
                        InfoRow("目标位置", t.targetAddress)
                        InfoRow("触发范围", "${t.locationRadius} 米")
                    }
                }
            }

            if (t.status == TaskStatus.COMPLETED || t.status == TaskStatus.FAILED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetTask(t) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("重新执行")
                    }
                }
            }

            Text("执行日志", style = MaterialTheme.typography.titleLarge)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                TimelineView(
                    logs = logs,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusTag(status: TaskStatus) {
    val (text, color) = when (status) {
        TaskStatus.PENDING -> "待执行" to TaskPending
        TaskStatus.EXECUTING -> "执行中" to TaskExecuting
        TaskStatus.COMPLETED -> "已完成" to TaskCompleted
        TaskStatus.FAILED -> "失败" to TaskFailed
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = MaterialTheme.typography.labelSmall.fontSize, fontWeight = FontWeight.Medium, color = color)
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
