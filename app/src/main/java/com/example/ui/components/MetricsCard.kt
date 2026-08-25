package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionMetrics
import com.example.model.ConnectionState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun MetricsCard(
    metrics: ConnectionMetrics,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, if (connectionState.isConnected) CyberEmerald.copy(alpha = 0.4f) else DarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("metrics_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Speed Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download Speed
                SpeedIndicator(
                    title = "DESCARGA",
                    speedText = if (connectionState.isConnected) metrics.formattedDownloadSpeed() else "0.0 B/s",
                    totalText = "Total: ${metrics.formattedTotalDownload()}",
                    icon = Icons.Filled.ArrowDownward,
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp)
                        .background(DarkBorder)
                )

                // Upload Speed
                SpeedIndicator(
                    title = "SUBIDA",
                    speedText = if (connectionState.isConnected) metrics.formattedUploadSpeed() else "0.0 B/s",
                    totalText = "Total: ${metrics.formattedTotalUpload()}",
                    icon = Icons.Filled.ArrowUpward,
                    accentColor = CyberEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DarkBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))

            // Secondary metrics row (Ping, Duration, IP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ping
                MetricMiniItem(
                    icon = Icons.Filled.NetworkCheck,
                    label = "Latencia",
                    value = if (connectionState.isConnected) "${metrics.pingMs} ms" else "-- ms",
                    accentColor = when {
                        !connectionState.isConnected -> TextMuted
                        metrics.pingMs < 45 -> CyberEmerald
                        metrics.pingMs < 100 -> CyberAmber
                        else -> CyberRed
                    }
                )

                // Duration
                MetricMiniItem(
                    icon = Icons.Filled.Timer,
                    label = "Tiempo Activo",
                    value = if (connectionState.isConnected) metrics.formattedDuration() else "00:00:00",
                    accentColor = if (connectionState.isConnected) CyberCyan else TextMuted
                )

                // IP
                MetricMiniItem(
                    icon = Icons.Filled.Dns,
                    label = "IP Asignada",
                    value = if (connectionState.isConnected) metrics.assignedIp else "0.0.0.0",
                    accentColor = if (connectionState.isConnected) TextWhite else TextMuted
                )
            }
        }
    }
}

@Composable
private fun SpeedIndicator(
    title: String,
    speedText: String,
    totalText: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                letterSpacing = 0.5.sp
            )
            Text(
                text = speedText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextWhite
            )
            Text(
                text = totalText,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun MetricMiniItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextGray
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = accentColor
        )
    }
}
