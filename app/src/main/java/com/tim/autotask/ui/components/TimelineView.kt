package com.tim.autotask.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tim.autotask.data.model.LogLevel
import com.tim.autotask.data.model.TaskLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(logs) { log ->
            TimelineItem(
                log = log,
                isLast = log == logs.last()
            )
        }
    }
}

@Composable
private fun TimelineItem(
    log: TaskLog,
    isLast: Boolean
) {
    val dotColor = when (log.level) {
        LogLevel.INFO -> MaterialTheme.colorScheme.primary
        LogLevel.SUCCESS -> Color(0xFF66BB6A)
        LogLevel.WARNING -> Color(0xFFFFA726)
        LogLevel.ERROR -> Color(0xFFEF5350)
    }

    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 24.dp, height = if (isLast) 24.dp else 48.dp)
        ) {
            val centerX = size.width / 2
            val dotRadius = 6.dp.toPx()
            val dotY = 12.dp.toPx()

            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(centerX, dotY)
            )

            if (!isLast) {
                drawLine(
                    color = lineColor,
                    start = Offset(centerX, dotY + dotRadius),
                    end = Offset(centerX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = formatTimestamp(log.timestamp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (log.level == LogLevel.ERROR) FontWeight.Bold else FontWeight.Normal,
                color = when (log.level) {
                    LogLevel.ERROR -> Color(0xFFEF5350)
                    LogLevel.SUCCESS -> Color(0xFF66BB6A)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
