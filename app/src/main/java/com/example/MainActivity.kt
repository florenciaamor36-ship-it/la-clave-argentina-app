package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ConfigScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            viewModel.importDtpFromUri(data)
        } else if (Intent.ACTION_SEND == action) {
            val streamUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (streamUri != null) {
                viewModel.importDtpFromUri(streamUri)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val config by viewModel.currentConfig.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val activeDtpRules by viewModel.activeDtpRules.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = CyberCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                // Tab 1: Inicio
                NavigationBarItem(
                    selected = selectedTab == AppTab.HOME,
                    onClick = { viewModel.selectTab(AppTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio", fontSize = 11.sp) },
                    colors = navigationItemColors(),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Tab 2: Logs
                NavigationBarItem(
                    selected = selectedTab == AppTab.LOGS,
                    onClick = { viewModel.selectTab(AppTab.LOGS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (logs.isNotEmpty() && connectionState.isConnecting) {
                                    Badge(containerColor = CyberCyan)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Terminal,
                                contentDescription = "Registro"
                            )
                        }
                    },
                    label = { Text("Registro", fontSize = 11.sp) },
                    colors = navigationItemColors(),
                    modifier = Modifier.testTag("nav_tab_logs")
                )

                // Tab 3: Ajustes / Conexión
                NavigationBarItem(
                    selected = selectedTab == AppTab.CONFIG,
                    onClick = { viewModel.selectTab(AppTab.CONFIG) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Ajustes"
                        )
                    },
                    label = { Text("Ajustes", fontSize = 11.sp) },
                    colors = navigationItemColors(),
                    modifier = Modifier.testTag("nav_tab_config")
                )

                // Tab 4: Guía de Uso
                NavigationBarItem(
                    selected = selectedTab == AppTab.GUIDE,
                    onClick = { viewModel.selectTab(AppTab.GUIDE) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Guía"
                        )
                    },
                    label = { Text("Guía", fontSize = 11.sp) },
                    colors = navigationItemColors(),
                    modifier = Modifier.testTag("nav_tab_guide")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_content_transition",
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.HOME -> {
                    HomeScreen(
                        config = config,
                        connectionState = connectionState,
                        metrics = metrics,
                        deviceId = viewModel.deviceId,
                        activeDtpRules = activeDtpRules,
                        onConnectClick = { viewModel.toggleConnection() },
                        onNavigateToConfig = { viewModel.selectTab(AppTab.CONFIG) },
                        onNavigateToGuide = { viewModel.selectTab(AppTab.GUIDE) },
                        onSelectTunnelType = { type -> viewModel.updateField(tunnelType = type) },
                        onCopyDeviceId = { viewModel.copyDeviceIdToClipboard() },
                        onShareDeviceId = { viewModel.shareDeviceId() },
                        onImportDtpUri = { uri -> viewModel.importDtpFromUri(uri) },
                        onImportDtpText = { text -> viewModel.importDtpFromText(text) }
                    )
                }
                AppTab.LOGS -> {
                    LogsScreen(
                        logs = logs,
                        onCopyLogs = { viewModel.copyLogsToClipboard() },
                        onShareLogs = { viewModel.shareLogs() },
                        onClearLogs = { viewModel.clearLogs() }
                    )
                }
                AppTab.CONFIG -> {
                    ConfigScreen(
                        config = config,
                        savedProfiles = savedProfiles,
                        activeDtpRules = activeDtpRules,
                        onUpdateField = { host, port, user, pass, payload, sni, proxyHost, proxyPort, tunnelType, udp, comp, dns, autoRec, wakeLock ->
                            viewModel.updateField(host, port, user, pass, payload, sni, proxyHost, proxyPort, tunnelType, udp, comp, dns, autoRec, wakeLock)
                        },
                        onInsertPayloadTag = { tag -> viewModel.insertPayloadTag(tag) },
                        onApplyPreset = { payload, port -> viewModel.applyPayloadPreset(payload, port) },
                        onSaveProfile = { name -> viewModel.saveCurrentAsProfile(name) },
                        onLoadProfile = { profile -> viewModel.loadProfile(profile) },
                        onDeleteProfile = { id -> viewModel.deleteProfile(id) },
                        onExportJson = { viewModel.exportConfigText() },
                        onImportJson = { json -> viewModel.importConfigText(json) },
                        onExportDtp = { pkg, shareImmediately -> viewModel.exportDtpPackage(pkg, shareImmediately) },
                        onImportDtpUri = { uri -> viewModel.importDtpFromUri(uri) },
                        onImportDtpText = { text -> viewModel.importDtpFromText(text) }
                    )
                }
                AppTab.GUIDE -> {
                    GuideScreen(
                        onNavigateToHome = { viewModel.selectTab(AppTab.HOME) },
                        onNavigateToConfig = { viewModel.selectTab(AppTab.CONFIG) },
                        onNavigateToLogs = { viewModel.selectTab(AppTab.LOGS) }
                    )
                }
            }
        }
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CyberCyan,
    selectedTextColor = CyberCyan,
    indicatorColor = CyberCyan.copy(alpha = 0.2f),
    unselectedIconColor = TextGray,
    unselectedTextColor = TextGray
)
