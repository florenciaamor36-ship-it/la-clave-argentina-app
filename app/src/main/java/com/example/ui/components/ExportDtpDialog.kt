package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DtpAccessRules
import com.example.model.DtpProfilePackage
import com.example.model.RestrictionType
import com.example.model.SshConfig
import com.example.ui.theme.CyberAmber
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
import com.example.util.DeviceIdentifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportDtpDialog(
    currentConfig: SshConfig,
    onDismiss: () -> Unit,
    onExport: (pkg: DtpProfilePackage, shareImmediately: Boolean) -> Unit
) {
    val context = LocalContext.current
    var profileName by remember { mutableStateOf(currentConfig.profileName.ifBlank { "Perfil TIENDA SSH" }) }
    var selectedRestriction by remember { mutableStateOf(RestrictionType.FREE) }

    // Expiration: Default to 1 hour from now
    var customExpirationTimestamp by remember {
        mutableLongStateOf(System.currentTimeMillis() + (60L * 60 * 1000))
    }
    var selectedPresetLabel by remember { mutableStateOf("1 hora") }

    // IDs
    var singleDeviceId by remember { mutableStateOf("") }
    var multiDeviceIdsText by remember { mutableStateOf("") }

    // Remote / VPS URL
    var vpsHwidUrl by remember { mutableStateOf("http://127.0.0.1:8080/hwids.txt") }

    // Options
    var isConfigLocked by remember { mutableStateOf(true) }
    var creatorNote by remember { mutableStateOf("") }

    val myDeviceId = remember { DeviceIdentifier.getDeviceId(context) }

    // Dialog picker triggers
    val openNativeDatePicker = {
        val currentCal = Calendar.getInstance().apply { timeInMillis = customExpirationTimestamp }
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = customExpirationTimestamp
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                customExpirationTimestamp = updatedCal.timeInMillis
                selectedPresetLabel = "Personalizado"
            },
            currentCal.get(Calendar.YEAR),
            currentCal.get(Calendar.MONTH),
            currentCal.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    val openNativeTimePicker = {
        val currentCal = Calendar.getInstance().apply { timeInMillis = customExpirationTimestamp }
        val dialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = customExpirationTimestamp
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
                customExpirationTimestamp = updatedCal.timeInMillis
                selectedPresetLabel = "Personalizado"
            },
            currentCal.get(Calendar.HOUR_OF_DAY),
            currentCal.get(Calendar.MINUTE),
            true
        )
        dialog.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = CyberCyan,
        textContentColor = TextWhite,
        modifier = Modifier.testTag("export_dtp_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Exportar Perfil .dtp", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Serialización y Restricciones de Acceso", fontSize = 11.sp, color = CyberCyan)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Profile Name
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Nombre del Perfil (.dtp)") },
                    singleLine = true,
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dtp_profile_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Restriction Selector Title
                Text(
                    text = "Tipo de Bloqueo / Licencia:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Selector Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RestrictionType.values().forEach { type ->
                        FilterChip(
                            selected = selectedRestriction == type,
                            onClick = { selectedRestriction = type },
                            label = {
                                Text(
                                    text = when (type) {
                                        RestrictionType.FREE -> "Libre"
                                        RestrictionType.EXPIRATION -> "Fecha y Hora"
                                        RestrictionType.SINGLE_ID -> "1 HWID Fijo"
                                        RestrictionType.MULTI_ID -> "Lista HWIDs"
                                        RestrictionType.VPS_HWID_CHECK -> "HWIDs en VPS"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedRestriction == type) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceCard,
                                labelColor = TextGray,
                                selectedContainerColor = if (type == RestrictionType.VPS_HWID_CHECK) CyberEmerald.copy(alpha = 0.25f) else CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = if (type == RestrictionType.VPS_HWID_CHECK) CyberEmerald else CyberCyan
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedRestriction == type) (if (type == RestrictionType.VPS_HWID_CHECK) CyberEmerald else CyberCyan) else DarkBorder,
                                enabled = true,
                                selected = selectedRestriction == type
                            ),
                            modifier = Modifier.testTag("restriction_chip_${type.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clear Detailed Explanation Card for Every Restriction
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (selectedRestriction) {
                            RestrictionType.VPS_HWID_CHECK -> CyberEmerald.copy(alpha = 0.10f)
                            RestrictionType.EXPIRATION -> CyberAmber.copy(alpha = 0.10f)
                            RestrictionType.FREE -> CyberCyan.copy(alpha = 0.08f)
                            else -> DarkSurfaceCard
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (selectedRestriction) {
                            RestrictionType.VPS_HWID_CHECK -> CyberEmerald.copy(alpha = 0.5f)
                            RestrictionType.EXPIRATION -> CyberAmber.copy(alpha = 0.5f)
                            RestrictionType.FREE -> CyberCyan.copy(alpha = 0.3f)
                            else -> DarkBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = when (selectedRestriction) {
                                    RestrictionType.VPS_HWID_CHECK -> CyberEmerald
                                    RestrictionType.EXPIRATION -> CyberAmber
                                    else -> CyberCyan
                                },
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedRestriction.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (selectedRestriction) {
                                    RestrictionType.VPS_HWID_CHECK -> CyberEmerald
                                    RestrictionType.EXPIRATION -> CyberAmber
                                    else -> CyberCyan
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedRestriction.description,
                            fontSize = 11.sp,
                            color = TextWhite.copy(alpha = 0.9f),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CONTEXTUAL CONFIGURATION BASED ON RESTRICTION
                when (selectedRestriction) {
                    RestrictionType.FREE -> {
                        // Free mode: no additional fields required
                    }

                    RestrictionType.EXPIRATION -> {
                        Text("Establecer Tiempo de Vigencia:", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Presets FlowRow (minutes, hours, days)
                        val presets = listOf(
                            "30 min" to (30L * 60 * 1000),
                            "1 hora" to (1L * 60 * 60 * 1000),
                            "2 horas" to (2L * 60 * 60 * 1000),
                            "6 horas" to (6L * 60 * 60 * 1000),
                            "12 horas" to (12L * 60 * 60 * 1000),
                            "1 día" to (1L * 24 * 60 * 60 * 1000),
                            "3 días" to (3L * 24 * 60 * 60 * 1000),
                            "7 días" to (7L * 24 * 60 * 60 * 1000),
                            "15 días" to (15L * 24 * 60 * 60 * 1000),
                            "30 días" to (30L * 24 * 60 * 60 * 1000)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            presets.forEach { (label, durationMs) ->
                                val isSelected = selectedPresetLabel == label
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CyberAmber.copy(alpha = 0.25f) else DarkSurfaceCard)
                                        .clickable {
                                            selectedPresetLabel = label
                                            customExpirationTimestamp = System.currentTimeMillis() + durationMs
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyberAmber else TextGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Manual Date & Time Picker Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { openNativeDatePicker() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Elegir Fecha", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { openNativeTimePicker() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Elegir Hora", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Expiration summary card
                        val now = System.currentTimeMillis()
                        val remainingMs = (customExpirationTimestamp - now).coerceAtLeast(0)
                        val remDays = remainingMs / (1000 * 60 * 60 * 24)
                        val remHours = (remainingMs / (1000 * 60 * 60)) % 24
                        val remMins = (remainingMs / (1000 * 60)) % 60

                        val remainingText = buildString {
                            if (remDays > 0) append("$remDays d ")
                            if (remHours > 0 || remDays > 0) append("$remHours h ")
                            append("$remMins min")
                        }

                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(customExpirationTimestamp))

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CyberAmber.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Timer, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Vence: $formattedDate",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Duración de prueba: $remainingText desde este momento.",
                                    fontSize = 11.sp,
                                    color = TextWhite
                                )
                            }
                        }
                    }

                    RestrictionType.SINGLE_ID -> {
                        Text("HWID Único Autorizado:", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = singleDeviceId,
                            onValueChange = { singleDeviceId = it.uppercase(Locale.ROOT) },
                            placeholder = { Text("ej: TS-89AB-CDEF-1234") },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = customTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dtp_single_id_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { singleDeviceId = myDeviceId },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.15f), contentColor = CyberCyan),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Usar mi HWID actual", fontSize = 11.sp)
                        }
                    }

                    RestrictionType.MULTI_ID -> {
                        Text("Lista Fija de HWIDs (en el archivo):", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = multiDeviceIdsText,
                            onValueChange = { multiDeviceIdsText = it },
                            placeholder = { Text("TS-AAAA-BBBB-CCCC\nTS-1111-2222-3333") },
                            minLines = 3,
                            maxLines = 5,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            colors = customTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dtp_multi_ids_input")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val parsedCount = multiDeviceIdsText.split(Regex("[,\\n]+")).filter { it.trim().isNotEmpty() }.size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dispositivos en lista: $parsedCount",
                                fontSize = 11.sp,
                                color = CyberEmerald
                            )
                            TextButton(
                                onClick = {
                                    multiDeviceIdsText = if (multiDeviceIdsText.isBlank()) myDeviceId else "$multiDeviceIdsText\n$myDeviceId"
                                }
                            ) {
                                Text("+ Añadir mi HWID", fontSize = 11.sp, color = CyberCyan)
                            }
                        }
                    }

                    RestrictionType.VPS_HWID_CHECK -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("URL o IP de la Lista de HWIDs en tu VPS:", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = vpsHwidUrl,
                            onValueChange = { vpsHwidUrl = it },
                            placeholder = { Text("http://TU_IP_VPS:8080/hwids.txt") },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            colors = customTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dtp_vps_url_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Explanatory badge
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CyberEmerald.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "★ UN SOLO ARCHIVO .DTP PARA TODOS LOS CLIENTES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Generas y compartes este archivo una sola vez. Cuando un usuario nuevo te compre acceso, solo agregas su HWID a la lista 'hwids.txt' o script en tu VPS. La app consulta tu VPS en tiempo real al conectar.",
                                    fontSize = 10.sp,
                                    color = TextWhite.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder)

                // Extra Options: Lock Config & Creator Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bloquear Configuración", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("Impide que el usuario edite o vea el payload/host", fontSize = 10.sp, color = TextGray)
                    }
                    Switch(
                        checked = isConfigLocked,
                        onCheckedChange = { isConfigLocked = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = DarkBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = creatorNote,
                    onValueChange = { creatorNote = it },
                    label = { Text("Nota / Mensaje para el usuario (Opcional)") },
                    placeholder = { Text("ej: Servidor VIP Gaming - Contacto: @SoporteSSH") },
                    singleLine = true,
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Export and Share button
                Button(
                    onClick = {
                        val pkg = buildPackage(
                            profileName, currentConfig, selectedRestriction,
                            customExpirationTimestamp, singleDeviceId, multiDeviceIdsText,
                            vpsHwidUrl, isConfigLocked, creatorNote
                        )
                        onExport(pkg, true)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dtp_export_and_share_button")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartir", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Save locally only
                Button(
                    onClick = {
                        val pkg = buildPackage(
                            profileName, currentConfig, selectedRestriction,
                            customExpirationTimestamp, singleDeviceId, multiDeviceIdsText,
                            vpsHwidUrl, isConfigLocked, creatorNote
                        )
                        onExport(pkg, false)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = TextWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dtp_save_local_button")
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

private fun buildPackage(
    profileName: String,
    currentConfig: SshConfig,
    restrictionType: RestrictionType,
    expirationTimestamp: Long,
    singleDeviceId: String,
    multiDeviceIdsText: String,
    remoteUrl: String,
    isLocked: Boolean,
    creatorNote: String
): DtpProfilePackage {
    val multiList = multiDeviceIdsText.split(Regex("[,\\n]+"))
        .map { it.trim().uppercase(Locale.ROOT) }
        .filter { it.isNotEmpty() }

    val rules = DtpAccessRules(
        restrictionType = restrictionType,
        expirationTimestamp = if (restrictionType == RestrictionType.EXPIRATION) expirationTimestamp else null,
        allowedDeviceId = if (restrictionType == RestrictionType.SINGLE_ID) singleDeviceId.trim().uppercase(Locale.ROOT) else null,
        allowedDeviceIds = if (restrictionType == RestrictionType.MULTI_ID) multiList else emptyList(),
        remoteValidationUrl = if (restrictionType == RestrictionType.VPS_HWID_CHECK) remoteUrl.trim() else null,
        isConfigLocked = isLocked,
        creatorNote = creatorNote,
        exportTimestamp = System.currentTimeMillis()
    )

    return DtpProfilePackage(
        profileName = profileName.ifBlank { "Perfil TIENDA SSH" },
        sshConfig = currentConfig.copy(profileName = profileName.ifBlank { currentConfig.profileName }),
        accessRules = rules
    )
}

@Composable
private fun customTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = DarkSurfaceCard,
    unfocusedContainerColor = DarkSurfaceCard,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedIndicatorColor = CyberCyan,
    unfocusedIndicatorColor = DarkBorder,
    focusedLabelColor = CyberCyan,
    unfocusedLabelColor = TextGray
)
