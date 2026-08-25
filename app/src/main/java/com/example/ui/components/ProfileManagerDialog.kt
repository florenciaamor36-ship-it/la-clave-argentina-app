package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SshConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun ProfileManagerDialog(
    savedProfiles: List<SshConfig>,
    onDismiss: () -> Unit,
    onSaveCurrent: (name: String) -> Unit,
    onLoadProfile: (SshConfig) -> Unit,
    onDeleteProfile: (id: String) -> Unit,
    onExportJson: () -> String,
    onImportJson: (json: String) -> Boolean,
    onImportDtpUri: (Uri) -> Boolean = { false },
    onImportDtpText: (String) -> Boolean = { false }
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Perfiles, 1: Guardar, 2: Importar .DTP, 3: JSON
    var newProfileName by remember { mutableStateOf("") }
    var importExportText by remember { mutableStateOf(onExportJson()) }
    var pastedDtpText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val ok = onImportDtpUri(uri)
            if (ok) {
                onDismiss()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("profile_manager_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestor de Configuraciones",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = TextGray)
                    }
                }

                // Sub-tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Lista (${savedProfiles.size})", fontSize = 11.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Guardar", fontSize = 11.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(".DTP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 2) CyberCyan else CyberEmerald, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { 
                            selectedTab = 3
                            importExportText = onExportJson()
                        },
                        text = { Text("JSON", fontSize = 11.sp, maxLines = 1) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> {
                        // List of profiles
                        if (savedProfiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay perfiles guardados", color = TextGray, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            ) {
                                items(savedProfiles) { profile ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                onLoadProfile(profile)
                                                onDismiss()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = profile.profileName.ifBlank { "Sin nombre" },
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite
                                                )
                                                Text(
                                                    text = "${profile.tunnelType.displayName} | ${profile.host}:${profile.port}",
                                                    fontSize = 11.sp,
                                                    color = TextGray
                                                )
                                            }

                                            Row {
                                                IconButton(onClick = {
                                                    onLoadProfile(profile)
                                                    onDismiss()
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.FileDownload,
                                                        contentDescription = "Cargar",
                                                        tint = CyberEmerald
                                                    )
                                                }
                                                IconButton(onClick = { onDeleteProfile(profile.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Eliminar",
                                                        tint = CyberRed
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Save current configuration
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Nombre para el perfil actual:",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = newProfileName,
                                onValueChange = { newProfileName = it },
                                placeholder = { Text("Ej: Servidor Personal 1") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceCard,
                                    unfocusedContainerColor = DarkSurfaceCard,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedIndicatorColor = CyberCyan,
                                    unfocusedIndicatorColor = DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_profile_name_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onSaveCurrent(newProfileName)
                                    selectedTab = 0
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_profile_button")
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("Guardar Configuración Actual", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    2 -> {
                        // Import .DTP (From device storage or paste base64)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Cargar archivo .dtp desde almacenamiento:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            filePickerLauncher.launch(arrayOf("*/*", "application/octet-stream"))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Seleccionar Archivo .dtp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "O pegar código / contenido .dtp:",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = pastedDtpText,
                                onValueChange = { pastedDtpText = it },
                                placeholder = { Text("TIENDA_SSH_DTP_V1::...") },
                                minLines = 3,
                                maxLines = 4,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceCard,
                                    unfocusedContainerColor = DarkSurfaceCard,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedIndicatorColor = CyberCyan,
                                    unfocusedIndicatorColor = DarkBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.getText()?.let {
                                            pastedDtpText = it.text
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pegar", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        if (pastedDtpText.isNotBlank()) {
                                            val ok = onImportDtpText(pastedDtpText)
                                            if (ok) {
                                                onDismiss()
                                            }
                                        }
                                    },
                                    enabled = pastedDtpText.isNotBlank(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = DarkBackground),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Importar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    3 -> {
                        // Import / Export JSON
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Exportar / Pegar JSON de Configuración:",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = importExportText,
                                onValueChange = { importExportText = it },
                                maxLines = 8,
                                minLines = 5,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceCard,
                                    unfocusedContainerColor = DarkSurfaceCard,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedIndicatorColor = CyberCyan,
                                    unfocusedIndicatorColor = DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("json_config_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        importExportText = onExportJson()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                    Text("Generar", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        if (onImportJson(importExportText)) {
                                            onDismiss()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = DarkBackground),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("import_json_button")
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                    Text("Importar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
