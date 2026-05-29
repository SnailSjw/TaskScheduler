package com.tim.autotask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.HorizontalDivider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tim.autotask.data.model.LogLevel
import com.tim.autotask.data.model.TaskLog
import com.tim.autotask.ui.theme.Amber
import com.tim.autotask.ui.theme.Amber80
import com.tim.autotask.ui.theme.Blue
import com.tim.autotask.ui.theme.Blue80
import com.tim.autotask.ui.theme.Green
import com.tim.autotask.ui.theme.Green80
import com.tim.autotask.ui.theme.Red
import com.tim.autotask.ui.theme.Red80
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DayLogGroup(
    val dateLabel: String,
    val weekday: String,
    val isToday: Boolean,
    val status: DayStatus,
    val logs: List<TaskLog>
)

private enum class DayStatus { SUCCESS, FAILED, RUNNING }

@Composable
fun TimelineView(
    logs: List<TaskLog>,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无执行日志",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val groups = remember(logs) { groupLogsByDay(logs) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { group ->
            DayGroupCard(group)
        }
    }
}

@Composable
private fun DayGroupCard(group: DayLogGroup) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = group.dateLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Text(
                text = group.weekday,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DayStatusBadge(group.status)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column {
                group.logs.forEachIndexed { index, log ->
                    LogRow(log)
                    if (index < group.logs.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayStatusBadge(status: DayStatus) {
    val (text, bg, fg) = when (status) {
        DayStatus.SUCCESS -> Triple("成功", Green.copy(alpha = 0.1f), Green)
        DayStatus.FAILED -> Triple("失败", Red.copy(alpha = 0.1f), Red)
        DayStatus.RUNNING -> Triple("运行中", Blue.copy(alpha = 0.1f), Blue)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun LogRow(log: TaskLog) {
    val isDark = isSystemInDarkTheme()

    val dotColor = when (log.level) {
        LogLevel.INFO -> if (isDark) Blue80 else Blue
        LogLevel.SUCCESS -> if (isDark) Green80 else Green
        LogLevel.WARNING -> if (isDark) Amber80 else Amber
        LogLevel.ERROR -> if (isDark) Red80 else Red
    }

    val tagBg = dotColor.copy(alpha = 0.1f)

    val msgColor = when (log.level) {
        LogLevel.ERROR -> dotColor
        LogLevel.SUCCESS -> dotColor
        LogLevel.WARNING -> dotColor
        LogLevel.INFO -> MaterialTheme.colorScheme.onSurface
    }

    val tagLabel = when (log.level) {
        LogLevel.INFO -> "INFO"
        LogLevel.SUCCESS -> "OK"
        LogLevel.WARNING -> "WARN"
        LogLevel.ERROR -> "ERROR"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimeOnly(log.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagBg)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = tagLabel,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dotColor,
                        letterSpacing = 0.3.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.message,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = msgColor,
                fontWeight = if (log.level == LogLevel.ERROR) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

private fun groupLogsByDay(logs: List<TaskLog>): List<DayLogGroup> {
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFmt = SimpleDateFormat("MM-dd", Locale.getDefault())
    val weekdayMap = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    val weekdayFmt = SimpleDateFormat("u", Locale.getDefault())
    val today = dateFmt.format(Date())

    val grouped = linkedMapOf<String, MutableList<TaskLog>>()
    for (log in logs) {
        val key = dateFmt.format(Date(log.timestamp))
        grouped.getOrPut(key) { mutableListOf() }.add(log)
    }

    return grouped.map { (dateStr, dayLogs) ->
        val date = dateFmt.parse(dateStr)!!
        val weekdayIdx = weekdayFmt.format(date).toInt() - 1
        val weekday = weekdayMap.getOrElse(weekdayIdx) { "" }

        val hasError = dayLogs.any { it.level == LogLevel.ERROR }
        val status = when {
            hasError -> DayStatus.FAILED
            dayLogs.any { it.level == LogLevel.SUCCESS } -> DayStatus.SUCCESS
            else -> DayStatus.RUNNING
        }

        DayLogGroup(
            dateLabel = dayFmt.format(date),
            weekday = weekday,
            isToday = dateStr == today,
            status = status,
            logs = dayLogs.sortedBy { it.timestamp }
        )
    }.sortedByDescending { it.logs.first().timestamp }
}

private fun formatTimeOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
