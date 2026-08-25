package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PayloadGeneratorDialog(
    onDismiss: () -> Unit,
    onApplyPayload: (payload: String, port: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("CONNECT") }
    var urlBugHost by remember { mutableStateOf("portal.claro.com.ar") }
    var selectedInjection by remember { mutableStateOf("Normal") } // Normal, Front-Inject, Back-Inject
    var splitMode by remember { mutableStateOf("Ninguno") } // Ninguno, Normal, Delay
    var keepAlive by remember { mutableStateOf(true) }
    var userAgent by remember { mutableStateOf(true) }

    fun generatePayloadString(): Pair<String, String> {
        val host = urlBugHost.ifBlank { "[host]" }
        val method = selectedMethod
        
        val payloadStr = when (selectedInjection) {
            "Front-Inject" -> {
                "$method http://$host/ HTTP/1.1[crlf]Host: $host[crlf]${if (userAgent) "User-Agent: [ua][crlf]" else ""}${if (keepAlive) "Connection: Keep-Alive[crlf]" else ""}[crlf]CONNECT [host_port] [protocol][crlf][crlf]"
            }
            "Back-Inject" -> {
                "CONNECT [host_port] [protocol][crlf]${if (splitMode == "Normal") "[split]" else ""}$method http://$host/ HTTP/1.1[crlf]Host: $host[crlf]${if (keepAlive) "Connection: Keep-Alive[crlf]" else ""}[crlf]"
            }
            else -> {
                // Normal
                "CONNECT [host_port] [protocol][crlf]Host: $host[crlf]X-Online-Host: $host[crlf]${if (userAgent) "User-Agent: [ua][crlf]" else ""}${if (keepAlive) "Connection: Keep-Alive[crlf]" else ""}[crlf][crlf]"
            }
        }
        val port = if (method == "CONNECT") "80" else "8080"
        return Pair(payloadStr, port)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("payload_generator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Generador de Payload",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = TextGray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                // URL / Host Bug
                Text(
                    text = "URL / Host Bug (SNI / Proxy Host):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = urlBugHost,
                    onValueChange = { urlBugHost = it },
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
                        .testTag("payload_bug_host_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Request Method
                Text(
                    text = "Método de Petición:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("CONNECT", "GET", "POST", "HEAD").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedMethod = method }
                                .padding(end = 12.dp)
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                            )
                            Text(text = method, fontSize = 12.sp, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Injection Type
                Text(
                    text = "Tipo de Inyección:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Normal", "Front-Inject", "Back-Inject").forEach { inj ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedInjection = inj }
                                .padding(end = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedInjection == inj,
                                onClick = { selectedInjection = inj },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                            )
                            Text(text = inj, fontSize = 11.sp, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Box
                Text(
                    text = "Vista Previa del Payload:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(10.dp)
                ) {
                    val (preview) = generatePayloadString()
                    Text(
                        text = preview,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val (payload, port) = generatePayloadString()
                            onApplyPayload(payload, port)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground),
                        modifier = Modifier.testTag("apply_generated_payload_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Aplicar Payload", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
