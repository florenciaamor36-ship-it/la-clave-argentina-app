package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ConnectionMetrics
import com.example.model.ConnectionState
import com.example.model.DtpAccessRules
import com.example.model.RestrictionType
import com.example.model.SshConfig
import com.example.model.TunnelType
import com.example.ui.components.ConnectButton
import com.example.ui.components.ImportDtpDialog
import com.example.ui.components.MetricsCard
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    config: SshConfig,
    connectionState: ConnectionState,
    metrics: ConnectionMetrics,
    deviceId: String,
    activeDtpRules: DtpAccessRules? = null,
    onConnectClick: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToGuide: () -> Unit = {},
    onSelectTunnelType: (TunnelType) -> Unit,
    onCopyDeviceId: () -> Unit,
    onShareDeviceId: () -> Unit,
    onImportDtpUri: (Uri) -> Boolean = { false },
    onImportDtpText: (String) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    var showImportDtpDialog by remember { mutableStateOf(false) }

    if (showImportDtpDialog) {
        ImportDtpDialog(
            onDismiss = { showImportDtpDialog = false },
            onImportUri = onImportDtpUri,
            onImportText = onImportDtpText
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar / Top Identity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(CyberCyan, CyberEmerald))
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Logo",
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "TIENDA SSH",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "VPN & HTTP Injector Client",
                        fontSize = 10.sp,
                        color = CyberCyan,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                // Quick Import .dtp button
                OutlinedButton(
                    onClick = { showImportDtpDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("home_import_dtp_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Importar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Quick Config Button
                IconButton(
                    onClick = onNavigateToConfig,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceCard)
                        .testTag("home_settings_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Ajustes",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Guide Button
                IconButton(
                    onClick = onNavigateToGuide,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceCard)
                        .testTag("home_guide_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Guía",
                        tint = CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. PERSISTENT DEVICE IDENTIFIER CARD (ID ÚNICO)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("device_id_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ID Único del Dispositivo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PERSISTENTE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Monospace ID Display with Copy & Share Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = deviceId,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald,
                        modifier = Modifier.testTag("device_id_text")
                    )

                    Row {
                        // Copiar al portapapeles
                        IconButton(
                            onClick = onCopyDeviceId,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("copy_device_id_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copiar al portapapeles",
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Compartir por texto
                        IconButton(
                            onClick = onShareDeviceId,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("share_device_id_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Compartir por texto",
                                tint = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active DTP License Info Banner (if loaded from .dtp)
        if (activeDtpRules != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (activeDtpRules.restrictionType) {
                        RestrictionType.EXPIRATION -> CyberAmber.copy(alpha = 0.12f)
                        RestrictionType.VPS_HWID_CHECK -> CyberEmerald.copy(alpha = 0.15f)
                        else -> CyberCyan.copy(alpha = 0.12f)
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (activeDtpRules.restrictionType) {
                        RestrictionType.EXPIRATION -> CyberAmber.copy(alpha = 0.5f)
                        RestrictionType.VPS_HWID_CHECK -> CyberEmerald.copy(alpha = 0.6f)
                        else -> CyberCyan.copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = when (activeDtpRules.restrictionType) {
                            RestrictionType.EXPIRATION -> CyberAmber
                            RestrictionType.VPS_HWID_CHECK -> CyberEmerald
                            else -> CyberCyan
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Licencia: ${activeDtpRules.restrictionType.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (activeDtpRules.expirationTimestamp != null) {
                            val expDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(activeDtpRules.expirationTimestamp))
                            Text(
                                text = "Vence: $expDate",
                                fontSize = 10.sp,
                                color = CyberAmber
                            )
                        } else if (activeDtpRules.creatorNote.isNotBlank()) {
                            Text(
                                text = activeDtpRules.creatorNote,
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Server Badge Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToConfig() }
                .testTag("active_server_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Dns,
                        contentDescription = null,
                        tint = if (connectionState.isConnected) CyberEmerald else CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = config.host.ifBlank { "Sin Host Configurado" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                        Text(
                            text = "Puerto: ${config.port} • ${config.tunnelType.displayName}",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (connectionState) {
                                ConnectionState.CONNECTED -> CyberEmerald.copy(alpha = 0.2f)
                                ConnectionState.CONNECTING,
                                ConnectionState.HANDSHAKE,
                                ConnectionState.AUTHENTICATING -> CyberAmber.copy(alpha = 0.2f)
                                ConnectionState.ERROR -> CyberRed.copy(alpha = 0.2f)
                                else -> DarkBorder
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (connectionState) {
                            ConnectionState.CONNECTED -> "ACTIVO"
                            ConnectionState.CONNECTING,
                            ConnectionState.HANDSHAKE,
                            ConnectionState.AUTHENTICATING -> "EN PROCESO"
                            ConnectionState.ERROR -> "ERROR"
                            else -> "INACTIVO"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (connectionState) {
                            ConnectionState.CONNECTED -> CyberEmerald
                            ConnectionState.CONNECTING,
                            ConnectionState.HANDSHAKE,
                            ConnectionState.AUTHENTICATING -> CyberAmber
                            ConnectionState.ERROR -> CyberRed
                            else -> TextGray
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CENTER ROUND CONNECT BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            ConnectButton(
                connectionState = connectionState,
                onClick = onConnectClick
            )
        }

        // Connection State Message
        Text(
            text = when (connectionState) {
                ConnectionState.CONNECTED -> "Conexión segura establecida con el túnel"
                ConnectionState.CONNECTING -> "Conectando al servidor y resolviendo proxy..."
                ConnectionState.HANDSHAKE -> "Negociando claves SSH y cifrado..."
                ConnectionState.AUTHENTICATING -> "Validando usuario y contraseña..."
                ConnectionState.DISCONNECTING -> "Cerrando túnel y limpiando sesión..."
                ConnectionState.ERROR -> "Fallo al conectar. Revisa tus credenciales o host"
                ConnectionState.DISCONNECTED -> "Listo para conectar"
            },
            fontSize = 12.sp,
            color = when (connectionState) {
                ConnectionState.CONNECTED -> CyberEmerald
                ConnectionState.CONNECTING,
                ConnectionState.HANDSHAKE,
                ConnectionState.AUTHENTICATING -> CyberAmber
                ConnectionState.ERROR -> CyberRed
                else -> TextMuted
            },
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // REAL-TIME METRICS COMPONENT
        MetricsCard(
            metrics = metrics,
            connectionState = connectionState
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
