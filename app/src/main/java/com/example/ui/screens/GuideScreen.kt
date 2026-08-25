package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class GuideTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val iconColor: Color,
    val steps: List<String>,
    val tips: List<String> = emptyList(),
    val codeExample: String? = null
)

@Composable
fun GuideScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var expandedTopicId by remember { mutableStateOf<String?>("ssh_http_80") }

    val categories = listOf("Todos", "Conexión", "Métodos de Túnel", "Payloads", "Archivos .DTP", "Seguridad HWID", "Logs & Red")

    val guideTopics = remember {
        listOf(
            GuideTopic(
                id = "ssh_http_80",
                title = "SSH + HTTP en Puerto 80 con Payload",
                subtitle = "Conexión a VPS usando Host, Puerto 80, Usuario, Clave y Payload",
                category = "Métodos de Túnel",
                icon = Icons.Filled.Http,
                iconColor = CyberCyan,
                steps = listOf(
                    "1. Ve a la pestaña 'Ajustes' o selecciona el chip 'SSH + HTTP' en la pantalla principal.",
                    "2. En 'Host / Servidor SSH', introduce la IP o dominio de tu VPS (ej: 198.51.100.1).",
                    "3. En 'Puerto', escribe '80' (o el puerto configurado en tu servidor web / Squid / Dropbear).",
                    "4. Llena tu 'Usuario SSH' y 'Contraseña SSH'.",
                    "5. En el campo 'Payload HTTP', escribe tu payload o presiona 'Generador' para crearlo automáticamente.",
                    "6. (Opcional) Si tu proveedor requiere proxy remoto, activa la casilla 'Proxy Remoto' y coloca su IP y puerto.",
                    "7. Ve a 'Inicio' y pulsa el botón grande 'CONECTAR'. En 'Registro' verás el handshake y túnel activo."
                ),
                tips = listOf(
                    "El puerto 80 es estándar para tráfico HTTP sin cifrar, ideal para inyección de cabeceras de operadoras.",
                    "Puedes usar el Generador de Payloads con método CONNECT y tag [host_port]."
                ),
                codeExample = "CONNECT [host_port] HTTP/1.1[crlf]Host: tudominio.com[crlf]X-Online-Host: tudominio.com[crlf]Connection: Keep-Alive[crlf][crlf]"
            ),
            GuideTopic(
                id = "quickstart",
                title = "Inicio Rápido: Cómo Conectar en 3 Pasos",
                subtitle = "Guía exprés para usuarios nuevos",
                category = "Conexión",
                icon = Icons.Filled.PlayArrow,
                iconColor = CyberEmerald,
                steps = listOf(
                    "Paso 1: Abre 'Ajustes' e introduce los datos de tu servidor VPS (Host, Puerto, Usuario y Contraseña).",
                    "Paso 2: Selecciona el método de túnel adecuado (SSH Directo, SSH+HTTP, SSH+SSL/TLS o SSL+Payload).",
                    "Paso 3: Vuelve a 'Inicio' y presiona 'CONECTAR'. Si el sistema te pide permiso para crear la VPN, acéptalo."
                ),
                tips = listOf(
                    "Si recibes un perfil de tu vendedor, usa directamente el botón 'Importar' en Inicio para no tener que configurar nada a mano."
                )
            ),
            GuideTopic(
                id = "tunnel_types",
                title = "Comparativa de Métodos de Túnel",
                subtitle = "Cuándo usar cada protocolo",
                category = "Métodos de Túnel",
                icon = Icons.Filled.VpnKey,
                iconColor = CyberCyan,
                steps = listOf(
                    "• SSH Directo: Conexión limpia sin proxies ni cabeceras. Ideal para puertos 22 o 443 directos.",
                    "• SSH + HTTP (Proxy/Payload): Realiza una inyección de cabeceras HTTP antes de autenticar por SSH. Se usa típicamente en puertos 80 u 8080.",
                    "• SSH + SSL / TLS / SNI: Encapsula el túnel dentro de una capa TLS segura. Requiere un 'SNI / Bug Host' (ej. m.facebook.com) y usa el puerto 443.",
                    "• SSH + Custom Payload: Inyección directa personalizada para redes con firewalls específicos.",
                    "• SSL + Payload: Combina túnel TLS con inyección de cabeceras HTTP internas."
                ),
                tips = listOf(
                    "Para operadoras que regalan WhatsApp o redes sociales, el método SSL/TLS con SNI suele ser el más efectivo."
                )
            ),
            GuideTopic(
                id = "payload_generator",
                title = "Generador de Payloads & Tags Soportados",
                subtitle = "Crea inyecciones HTTP avanzadas para tu operadora",
                category = "Payloads",
                icon = Icons.Filled.Code,
                iconColor = CyberAmber,
                steps = listOf(
                    "1. En la pantalla de Ajustes, pulsa el botón 'Generador' junto al campo Payload.",
                    "2. Elige el método HTTP (CONNECT, GET, POST, HEAD, OPTIONS).",
                    "3. Escribe el Host Bug (ej: portal.operador.com).",
                    "4. Elige el tipo de inyección (Normal, Front Inject, Back Inject o Direct).",
                    "5. Marca las opciones deseadas (Keep-Alive, User-Agent, Split, Dual-Connect).",
                    "6. Pulsa 'Generar Payload' para insertarlo directamente en tu configuración."
                ),
                tips = listOf(
                    "Tags soportados en la app:",
                    "  • [host_port] : Reemplaza con Host:Puerto del servidor SSH.",
                    "  • [host] : IP o dominio del servidor SSH.",
                    "  • [port] : Puerto del servidor SSH.",
                    "  • [crlf] : Salto de línea CRLF (\\r\\n).",
                    "  • [ua] : User-Agent emulado de Android.",
                    "  • [protocol] : Protocolo HTTP/1.1 o HTTP/1.0."
                ),
                codeExample = "GET / HTTP/1.1[crlf]Host: operador.com[crlf]Connection: Upgrade[crlf]Upgrade: websocket[crlf][crlf]CONNECT [host_port] HTTP/1.1[crlf][crlf]"
            ),
            GuideTopic(
                id = "dtp_files",
                title = "Archivos .DTP: Exportar, Importar y Proteger",
                subtitle = "Todo sobre el formato seguro de Tienda SSH",
                category = "Archivos .DTP",
                icon = Icons.Filled.FileUpload,
                iconColor = CyberEmerald,
                steps = listOf(
                    "• ¿Qué es .DTP? Es un formato seguro que empaqueta toda tu configuración en Base64 cifrado para compartir con clientes o amigos.",
                    "• Cómo Exportar: En Ajustes, ve a 'Exportar .dtp'. Podrás asignarle un nombre y elegir reglas de protección.",
                    "• Bloqueos disponibles:",
                    "   - Libre: Sin límites, cualquiera puede usarlo.",
                    "   - Límite de Tiempo: Fija fecha y hora exacta de vencimiento (con DatePicker y TimePicker).",
                    "   - Dispositivo Único: Solo el HWID colocado podrá abrir y conectar el perfil.",
                    "   - Lista de Dispositivos (VPS): Permite agregar múltiples HWIDs autorizados.",
                    "• Cómo Importar: Pulsa 'Importar' en Inicio o Ajustes y selecciona el archivo .dtp o pega el código Base64."
                ),
                tips = listOf(
                    "Los archivos .dtp ocultan contraseñas y datos sensibles para que los usuarios no puedan modificarlos si activas el bloqueo."
                )
            ),
            GuideTopic(
                id = "hwid_security",
                title = "ID Único de Dispositivo (HWID) y Licencias",
                subtitle = "Cómo vender o licenciar perfiles por dispositivo",
                category = "Seguridad HWID",
                icon = Icons.Filled.Fingerprint,
                iconColor = CyberCyan,
                steps = listOf(
                    "1. Cada teléfono tiene un 'ID Único (HWID)' visible en la pantalla de Inicio.",
                    "2. El cliente copia su HWID con el botón 'Copiar' o 'Compartir' y se lo envía al creador del perfil.",
                    "3. El creador abre 'Exportar .dtp', elige 'Un Dispositivo Específico' o 'Múltiples HWIDs' y pega el HWID del cliente.",
                    "4. Se exporta el archivo .dtp y se entrega al cliente.",
                    "5. Si otra persona intenta usar ese mismo archivo, la app bloqueará la conexión por HWID no autorizado."
                ),
                tips = listOf(
                    "El HWID es persistente en el dispositivo y no cambia al reiniciar."
                )
            ),
            GuideTopic(
                id = "logs_troubleshooting",
                title = "Lectura de Logs & Solución de Problemas",
                subtitle = "Cómo identificar y resolver errores comunes",
                category = "Logs & Red",
                icon = Icons.Filled.Terminal,
                iconColor = CyberRed,
                steps = listOf(
                    "• Estado 'Conectando...' se congela: Verifica que el Host y Puerto sean correctos y que tu VPS esté encendida.",
                    "• Error 'Autenticación fallida': Revisa el usuario y la contraseña SSH en Ajustes.",
                    "• Error 'HTTP 403 / 502 / Host no responde': El bug host o payload fue rechazado por el proxy o la operadora.",
                    "• 'Perfil Vencido': El archivo .dtp expiró según la fecha establecida por su creador.",
                    "• 'Dispositivo No Autorizado': El archivo .dtp fue creado para otro HWID distinto al tuyo."
                ),
                tips = listOf(
                    "En la pestaña 'Registro' puedes pulsar 'Copiar Logs' o 'Compartir' para enviar el reporte técnico de error a soporte."
                )
            ),
            GuideTopic(
                id = "advanced_options",
                title = "Opciones Avanzadas: UDP, DNS y WakeLock",
                subtitle = "Mejora la estabilidad y rendimiento del túnel",
                category = "Logs & Red",
                icon = Icons.Filled.Speed,
                iconColor = CyberAmber,
                steps = listOf(
                    "• UDP Forwarding (BadVPN): Permite el tráfico UDP a través del túnel (necesario para llamadas de WhatsApp, Discord y juegos en línea).",
                    "• Compresión de Datos: Reduce el consumo de datos comprimiendo el tráfico SSH.",
                    "• DNS Personalizado: Puedes usar Google DNS (8.8.8.8) o Cloudflare (1.1.1.1) para evitar bloqueos por DNS de tu operadora.",
                    "• WakeLock (Bloqueo de CPU): Evita que el ahorro de batería de Android mate la conexión cuando la pantalla está apagada.",
                    "• Reconexión Automática: Si la señal cae o cambia de torre celular, la app reintentará conectarse automáticamente."
                )
            )
        )
    }

    val filteredTopics = guideTopics.filter { topic ->
        val matchesCategory = selectedCategory == "Todos" || topic.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                topic.title.contains(searchQuery, ignoreCase = true) ||
                topic.subtitle.contains(searchQuery, ignoreCase = true) ||
                topic.steps.any { it.contains(searchQuery, ignoreCase = true) }
        matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("guide_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Guía de Uso",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Manual completo y resolución de dudas",
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                }
            }

            // Quick shortcut to Config
            Button(
                onClick = onNavigateToConfig,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = CyberCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ir a Ajustes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar tema, puerto 80, payload, .dtp...", fontSize = 12.sp, color = TextGray) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = CyberCyan, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurfaceCard,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedIndicatorColor = CyberCyan,
                unfocusedIndicatorColor = DarkBorder
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guide_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan,
                        containerColor = DarkSurfaceCard,
                        labelColor = TextGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = CyberCyan,
                        borderColor = DarkBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Guide List
        if (filteredTopics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = TextGray, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No se encontraron temas con '$searchQuery'", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTopics, key = { it.id }) { topic ->
                    val isExpanded = expandedTopicId == topic.id
                    GuideTopicCard(
                        topic = topic,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedTopicId = if (isExpanded) null else topic.id
                        }
                    )
                }

                // Bottom Callout for Quick Actions
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("¿Listo para probar tu conexión?", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aplica los ajustes de tu VPS y conecta con un solo toque desde la pantalla principal.",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToHome,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = DarkBackground),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ir a Inicio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = onNavigateToLogs,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ver Registro", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideTopicCard(
    topic: GuideTopic,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) topic.iconColor.copy(alpha = 0.6f) else DarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("guide_card_${topic.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(topic.iconColor.copy(alpha = 0.15f))
                            .border(1.dp, topic.iconColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = topic.icon,
                            contentDescription = null,
                            tint = topic.iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = topic.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) topic.iconColor else TextWhite
                        )
                        Text(
                            text = topic.subtitle,
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }

                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        tint = if (isExpanded) topic.iconColor else TextGray
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Instrucciones Paso a Paso:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    topic.steps.forEach { step ->
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            color = TextWhite,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    if (topic.codeExample != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ejemplo de Payload / Formato:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = topic.codeExample,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = CyberEmerald,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (topic.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CyberCyan.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Consejos Útiles:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                topic.tips.forEach { tip ->
                                    Text(
                                        text = tip,
                                        fontSize = 11.sp,
                                        color = TextWhite,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
