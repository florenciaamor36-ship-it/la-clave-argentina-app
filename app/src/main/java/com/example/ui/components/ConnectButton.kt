package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberEmeraldDim
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated

@Composable
fun ConnectButton(
    connectionState: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (connectionState.isConnecting || connectionState.isConnected) 1.12f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val activeColor = when (connectionState) {
        ConnectionState.CONNECTED -> CyberEmerald
        ConnectionState.CONNECTING,
        ConnectionState.HANDSHAKE,
        ConnectionState.AUTHENTICATING -> CyberAmber
        ConnectionState.DISCONNECTING -> CyberPurple
        ConnectionState.ERROR -> CyberRed
        ConnectionState.DISCONNECTED -> CyberCyan
    }

    val buttonLabel = when (connectionState) {
        ConnectionState.CONNECTED -> "DESCONECTAR"
        ConnectionState.CONNECTING -> "CONECTANDO..."
        ConnectionState.HANDSHAKE -> "HANDSHAKE..."
        ConnectionState.AUTHENTICATING -> "AUTENTICANDO..."
        ConnectionState.DISCONNECTING -> "CERRANDO..."
        ConnectionState.ERROR -> "REINTENTAR"
        ConnectionState.DISCONNECTED -> "CONECTAR"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(220.dp)
            .testTag("round_connect_button_container")
    ) {
        // Outer pulsing aura
        if (connectionState.isConnected || connectionState.isConnecting) {
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = pulseAlpha))
            )
        }

        // Spinning progress ring when connecting
        if (connectionState.isConnecting) {
            Canvas(
                modifier = Modifier
                    .size(190.dp)
                    .rotate(rotationAngle)
            ) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            activeColor.copy(alpha = 0.3f),
                            activeColor
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Main Round Button Surface
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(172.dp)
                .shadow(
                    elevation = if (connectionState.isConnected) 20.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = activeColor.copy(alpha = 0.5f),
                    spotColor = activeColor
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DarkSurfaceElevated,
                            DarkSurface
                        )
                    )
                )
                .drawBehind {
                    // Outer neon border
                    drawCircle(
                        color = activeColor,
                        radius = size.minDimension / 2 - 2.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Inner subtle glow circle
                    drawCircle(
                        color = activeColor.copy(alpha = 0.15f),
                        radius = size.minDimension / 2 - 8.dp.toPx()
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = activeColor),
                    onClick = onClick
                )
                .testTag("round_connect_button")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Central Icon
                when (connectionState) {
                    ConnectionState.CONNECTED -> {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Conectado",
                            tint = CyberEmerald,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    ConnectionState.CONNECTING,
                    ConnectionState.HANDSHAKE,
                    ConnectionState.AUTHENTICATING -> {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Conectando",
                            tint = CyberAmber,
                            modifier = Modifier
                                .size(46.dp)
                                .rotate(rotationAngle)
                        )
                    }
                    ConnectionState.ERROR -> {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = "Error",
                            tint = CyberRed,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Filled.PowerSettingsNew,
                            contentDescription = "Conectar",
                            tint = CyberCyan,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buttonLabel,
                    color = activeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
