package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DtpAccessRules
import com.example.model.DtpProfilePackage
import com.example.model.SshConfig
import com.example.model.TunnelType
import com.example.ui.components.ExportDtpDialog
import com.example.ui.components.ImportDtpDialog
import com.example.ui.components.PayloadGeneratorDialog
import com.example.ui.components.ProfileManagerDialog
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfigScreen(
    config: SshConfig,
    savedProfiles: List<SshConfig>,
    activeDtpRules: DtpAccessRules? = null,
    onUpdateField: (
        host: String?,
        port: String?,
        user: String?,
        pass: String?,
        payload: String?,
        sni: String?,
        proxyHost: String?,
        proxyPort: String?,
        tunnelType: TunnelType?,
        enableUdp: Boolean?,
        enableCompression: Boolean?,
        dns: String?,
        autoReconnect: Boolean?,
        wakeLock: Boolean?
    ) -> Unit,
    onInsertPayloadTag: (tag: String) -> Unit,
    onApplyPreset: (payload: String, port: String) -> Unit,
    onSaveProfile: (name: String) -> Unit,
    onLoadProfile: (SshConfig) -> Unit,
    onDeleteProfile: (id: String) -> Unit,
    onExportJson: () -> String,
    onImportJson: (json: String) -> Boolean,
    onExportDtp: (pkg: DtpProfilePackage, shareImmediately: Boolean) -> Unit,
    onImportDtpUri: (Uri) -> Boolean = { false },
    onImportDtpText: (String) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var showPayloadGenerator by remember { mutableStateOf(false) }
    var showProfileManager by remember { mutableStateOf(false) }
    var showExportDtpDialog by remember { mutableStateOf(false) }
    var showImportDtpDialog by remember { mutableStateOf(false) }

    if (showImportDtpDialog) {
        ImportDtpDialog(
            onDismiss = { showImportDtpDialog = false },
            onImportUri = onImportDtpUri,
            onImportText = onImportDtpText
        )
    }

    if (showExportDtpDialog) {
        ExportDtpDialog(
            currentConfig = config,
            onDismiss = { showExportDtpDialog = false },
            onExport = { pkg, shareImmediately ->
                showExportDtpDialog = false
                onExportDtp(pkg, shareImmediately)
            }
        )
    }

    if (showPayloadGenerator) {
        PayloadGeneratorDialog(
            onDismiss = { showPayloadGenerator = false },
            onApplyPayload = { payload, port ->
                onApplyPreset(payload, port)
                showPayloadGenerator = false
            }
        )
    }

    if (showProfileManager) {
        ProfileManagerDialog(
            savedProfiles = savedProfiles,
            onDismiss = { showProfileManager = false },
            onSaveCurrent = onSaveProfile,
            onLoadProfile = onLoadProfile,
            onDeleteProfile = onDeleteProfile,
            onExportJson = onExportJson,
            onImportJson = onImportJson,
            onImportDtpUri = onImportDtpUri,
            onImportDtpText = onImportDtpText
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ajustes de Conexión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1
                )
                Text(
                    text = "Host, Payload y Serialización .dtp",
                    fontSize = 11.sp,
                    color = TextGray,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Profile Manager trigger
            Button(
                onClick = { showProfileManager = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = TextWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.testTag("manage_profiles_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Perfiles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Action Banner for Quick .dtp Import & Export
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Perfiles Protegidos (.dtp)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Importa perfiles externos o exporta con fecha límite y control de HWIDs.",
                    fontSize = 11.sp,
                    color = TextGray,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showImportDtpDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("banner_import_dtp_button")
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Importar .dtp", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    Button(
                        onClick = { showExportDtpDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("banner_export_dtp_button")
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar .dtp", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // 1. MODO DE TÚNEL / PROTOCOLO
        Text(
            text = "Modo de Conexión:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyberCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TunnelType.values().forEach { type ->
                FilterChip(
                    selected = config.tunnelType == type,
                    onClick = {
                        onUpdateField(
                            null,
                            if (config.port == config.tunnelType.defaultPort) type.defaultPort else null,
                            null, null, null, null, null, null,
                            type,
                            null, null, null, null, null
                        )
                    },
                    label = { Text(type.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceCard,
                        labelColor = TextGray,
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (config.tunnelType == type) CyberCyan else DarkBorder,
                        enabled = true,
                        selected = config.tunnelType == type
                    ),
                    modifier = Modifier.testTag("tunnel_chip_${type.name}")
                )
            }
        }

        // 2. SERVIDOR SSH (HOST & PUERTO)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Dns,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Servidor SSH",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = config.host,
                        onValueChange = { onUpdateField(it, null, null, null, null, null, null, null, null, null, null, null, null, null) },
                        label = { Text("Host / IP SSH") },
                        placeholder = { Text("ej: 198.51.100.24") },
                        singleLine = true,
                        colors = customTextFieldColors(),
                        modifier = Modifier
                            .weight(2.2f)
                            .testTag("config_host_input")
                    )

                    OutlinedTextField(
                        value = config.port,
                        onValueChange = { onUpdateField(null, it, null, null, null, null, null, null, null, null, null, null, null, null) },
                        label = { Text("Puerto") },
                        placeholder = { Text("22") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = customTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("config_port_input")
                    )
                }
            }
        }

        // 3. CREDENCIALES (USUARIO & CONTRASEÑA)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VpnKey,
                        contentDescription = null,
                        tint = CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Autenticación SSH",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = config.username,
                    onValueChange = { onUpdateField(null, null, it, null, null, null, null, null, null, null, null, null, null, null) },
                    label = { Text("Usuario SSH") },
                    placeholder = { Text("usuario_ssh") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("config_user_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = config.password,
                    onValueChange = { onUpdateField(null, null, null, it, null, null, null, null, null, null, null, null, null, null) },
                    label = { Text("Contraseña SSH") },
                    placeholder = { Text("••••••••") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Mostrar contraseña",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("config_pass_input")
                )
            }
        }

        // 4. PAYLOAD HTTP / HEADERS (If HTTP Custom or SSL Payload)
        AnimatedVisibility(
            visible = config.tunnelType == TunnelType.SSH_HTTP_CUSTOM || config.tunnelType == TunnelType.SSL_PAYLOAD
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Http,
                                contentDescription = null,
                                tint = CyberAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Payload HTTP Personalizado",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Button(
                            onClick = { showPayloadGenerator = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.15f), contentColor = CyberCyan),
                            modifier = Modifier.testTag("open_payload_generator_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Insert Tags
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("[host_port]", "[host]", "[protocol]", "[crlf]", "[split]", "[ua]").forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkBackground)
                                    .clickable { onInsertPayloadTag(tag) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = config.payload,
                        onValueChange = { onUpdateField(null, null, null, null, it, null, null, null, null, null, null, null, null, null) },
                        maxLines = 5,
                        minLines = 3,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = customTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_payload_input")
                    )
                }
            }
        }

        // 5. SNI / BUG HOST (If SSL or SSL Payload)
        AnimatedVisibility(
            visible = config.tunnelType == TunnelType.SSL_TLS || config.tunnelType == TunnelType.SSL_PAYLOAD
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SNI / Host Bug (SSL)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = config.sniBugHost,
                        onValueChange = { onUpdateField(null, null, null, null, null, it, null, null, null, null, null, null, null, null) },
                        label = { Text("Server Name Indication (SNI)") },
                        placeholder = { Text("ej: m.facebook.com") },
                        singleLine = true,
                        colors = customTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_sni_input")
                    )
                }
            }
        }

        // 6. PROXY REMOTO (Squid / HTTP Proxy)
        AnimatedVisibility(
            visible = config.tunnelType == TunnelType.SSH_HTTP_CUSTOM || config.tunnelType == TunnelType.SSH_PROXY
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Router,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Proxy Remoto / Squid (Opcional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = config.proxyHost,
                            onValueChange = { onUpdateField(null, null, null, null, null, null, it, null, null, null, null, null, null, null) },
                            label = { Text("IP / Host Proxy") },
                            placeholder = { Text("198.51.100.24") },
                            singleLine = true,
                            colors = customTextFieldColors(),
                            modifier = Modifier
                                .weight(2.2f)
                                .testTag("config_proxy_host_input")
                        )

                        OutlinedTextField(
                            value = config.proxyPort,
                            onValueChange = { onUpdateField(null, null, null, null, null, null, null, it, null, null, null, null, null, null) },
                            label = { Text("Puerto") },
                            placeholder = { Text("8080") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = customTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("config_proxy_port_input")
                        )
                    }
                }
            }
        }

        // 7. OPCIONES AVANZADAS TOGGLE
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdvancedOptions = !showAdvancedOptions }
                .testTag("toggle_advanced_options")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustes Avanzados de Red",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Icon(
                    imageVector = if (showAdvancedOptions) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = CyberCyan
                )
            }
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // UDP Forwarding
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UDP Forwarding (BadVPN)", fontSize = 13.sp, color = TextWhite)
                            Text("Habilita juegos online y llamadas VoIP", fontSize = 10.sp, color = TextGray)
                        }
                        Switch(
                            checked = config.enableUdpForwarding,
                            onCheckedChange = { onUpdateField(null, null, null, null, null, null, null, null, null, it, null, null, null, null) },
                            colors = switchColors()
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)

                    // SSH Compression
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compresión de Datos SSH", fontSize = 13.sp, color = TextWhite)
                            Text("Ahorra datos y mejora la velocidad", fontSize = 10.sp, color = TextGray)
                        }
                        Switch(
                            checked = config.enableCompression,
                            onCheckedChange = { onUpdateField(null, null, null, null, null, null, null, null, null, null, it, null, null, null) },
                            colors = switchColors()
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)

                    // Auto-reconnect
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reconexión Automática", fontSize = 13.sp, color = TextWhite)
                            Text("Reconectar si la señal se interrumpe", fontSize = 10.sp, color = TextGray)
                        }
                        Switch(
                            checked = config.autoReconnect,
                            onCheckedChange = { onUpdateField(null, null, null, null, null, null, null, null, null, null, null, null, it, null) },
                            colors = switchColors()
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)

                    // Custom DNS
                    Text("Servidores DNS:", fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = config.customDns,
                        onValueChange = { onUpdateField(null, null, null, null, null, null, null, null, null, null, null, it, null, null) },
                        placeholder = { Text("1.1.1.1, 1.0.0.1") },
                        singleLine = true,
                        colors = customTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun customTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = DarkBackground,
    unfocusedContainerColor = DarkBackground,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedIndicatorColor = CyberCyan,
    unfocusedIndicatorColor = DarkBorder,
    focusedLabelColor = CyberCyan,
    unfocusedLabelColor = TextGray
)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = DarkBackground,
    checkedTrackColor = CyberCyan,
    uncheckedThumbColor = TextGray,
    uncheckedTrackColor = DarkBorder
)
