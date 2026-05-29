package com.tim.autotask.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tim.autotask.data.model.RepeatType
import com.tim.autotask.data.model.TaskType
import com.tim.autotask.ui.components.AppInfo
import com.tim.autotask.ui.components.AppPickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMapPicker: (Double, Double) -> Unit,
    mapPickerResult: MapPickerResult?,
    viewModel: CreateTaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var taskName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.SCHEDULED_LAUNCH) }
    var scheduledTime by remember { mutableLongStateOf(calendar.timeInMillis + 3600_000) }
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var showRepeatMenu by remember { mutableStateOf(false) }

    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }

    var locationRadius by remember { mutableFloatStateOf(100f) }

    val locationResult = mapPickerResult

    fun pickDate() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                scheduledTime = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickTime() {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.timeInMillis = scheduledTime
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                scheduledTime = calendar.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                selectedApp = app
                showAppPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建任务") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("任务名称") },
                placeholder = { Text("例如：每天早上打开钉钉") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("任务类型", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeChip(
                    text = "定时启动",
                    selected = selectedType == TaskType.SCHEDULED_LAUNCH,
                    onClick = { selectedType = TaskType.SCHEDULED_LAUNCH }
                )
                TypeChip(
                    text = "定位触发",
                    selected = selectedType == TaskType.LOCATION_TRIGGERED,
                    onClick = { selectedType = TaskType.LOCATION_TRIGGERED }
                )
            }

            Text("执行时间", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = ::pickDate,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatDate(scheduledTime))
                }
                Button(
                    onClick = ::pickTime,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatTime(scheduledTime))
                }
            }

            Box {
                Button(
                    onClick = { showRepeatMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重复：${repeatTypeLabel(repeatType)}")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showRepeatMenu,
                    onDismissRequest = { showRepeatMenu = false }
                ) {
                    RepeatType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(repeatTypeLabel(type)) },
                            onClick = {
                                repeatType = type
                                showRepeatMenu = false
                            }
                        )
                    }
                }
            }

            Text("目标应用", style = MaterialTheme.typography.titleLarge)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPicker = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    if (selectedApp != null) {
                        Column {
                            Text(selectedApp!!.label, fontWeight = FontWeight.Medium)
                            Text(
                                selectedApp!!.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("点击选择应用", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (selectedType == TaskType.LOCATION_TRIGGERED) {
                Text("目标位置", style = MaterialTheme.typography.titleLarge)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigateToMapPicker(
                                locationResult?.latitude ?: 39.9042,
                                locationResult?.longitude ?: 116.4074
                            )
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        if (locationResult != null) {
                            Column {
                                Text(
                                    locationResult.address,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "(${String.format("%.4f", locationResult.latitude)}, ${String.format("%.4f", locationResult.longitude)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text("点击在地图上选择位置", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (locationResult != null) {
                    Column {
                        Text(
                            "触发范围：${locationRadius.toInt()} 米",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Slider(
                            value = locationRadius,
                            onValueChange = { locationRadius = it },
                            valueRange = 50f..500f,
                            steps = 8
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (taskName.isBlank() || selectedApp == null) return@Button
                    viewModel.createTask(
                        name = taskName,
                        type = selectedType,
                        scheduledTime = scheduledTime,
                        repeatType = repeatType,
                        targetPackageName = selectedApp!!.packageName,
                        targetAppLabel = selectedApp!!.label,
                        targetLatitude = locationResult?.latitude,
                        targetLongitude = locationResult?.longitude,
                        targetAddress = locationResult?.address,
                        locationRadius = locationRadius.toInt(),
                        onSuccess = onNavigateBack
                    )
                },
                enabled = taskName.isNotBlank() && selectedApp != null &&
                        (selectedType == TaskType.SCHEDULED_LAUNCH || locationResult != null),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存任务", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun repeatTypeLabel(type: RepeatType): String = when (type) {
    RepeatType.NONE -> "不重复"
    RepeatType.DAILY -> "每天"
    RepeatType.WEEKLY -> "每周"
}
