package com.aerotech.aerocode.feature.preview

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.viewinterop.AndroidView
import com.aerotech.aerocode.domain.preview.DeviceType
import com.aerotech.aerocode.domain.preview.PreviewConfig
import com.aerotech.aerocode.domain.visual.ComponentNode
import com.aerotech.aerocode.ui.theme.AeroTheme

enum class PreviewRunnerType {
    NATIVE_COMPOSE,
    WEB_JAVASCRIPT
}

@Composable
fun PreviewScreen(
    rootNode: ComponentNode,
    config: PreviewConfig,
    onConfigChange: (PreviewConfig) -> Unit,
    onSelectComponent: (ComponentNode) -> Unit,
    onSwitchToVisual: () -> Unit,
    onSwitchToCode: (line: Int) -> Unit,
    onRefresh: () -> Unit,
    sourceCode: String = "",
    activeFilePath: String = "",
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors
    var deviceMenuExpanded by remember { mutableStateOf(false) }
    var zoomMenuExpanded by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<ComponentNode?>(null) }

    val isWebFile = activeFilePath.endsWith(".html") || activeFilePath.endsWith(".htm") || activeFilePath.endsWith(".js")
    var runnerType by remember(activeFilePath) {
        mutableStateOf(if (isWebFile) PreviewRunnerType.WEB_JAVASCRIPT else PreviewRunnerType.NATIVE_COMPOSE)
    }

    val webConsoleLogs = remember { mutableStateListOf<String>() }
    var showWebConsole by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Preview Control Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Runner Selector (Native vs Web/JS)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (runnerType == PreviewRunnerType.NATIVE_COMPOSE) colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (runnerType == PreviewRunnerType.NATIVE_COMPOSE) androidx.compose.foundation.BorderStroke(1.dp, colors.primary) else null,
                        modifier = Modifier.clickable { runnerType = PreviewRunnerType.NATIVE_COMPOSE }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = if (runnerType == PreviewRunnerType.NATIVE_COMPOSE) colors.primary else colors.textSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compose", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (runnerType == PreviewRunnerType.NATIVE_COMPOSE) colors.primary else colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (runnerType == PreviewRunnerType.WEB_JAVASCRIPT) colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (runnerType == PreviewRunnerType.WEB_JAVASCRIPT) androidx.compose.foundation.BorderStroke(1.dp, colors.primary) else null,
                        modifier = Modifier.clickable { runnerType = PreviewRunnerType.WEB_JAVASCRIPT }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = if (runnerType == PreviewRunnerType.WEB_JAVASCRIPT) colors.primary else colors.textSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Web & JS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (runnerType == PreviewRunnerType.WEB_JAVASCRIPT) colors.primary else colors.textSecondary)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Device Picker
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
                            modifier = Modifier.clickable { deviceMenuExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(config.device.label.substringBefore(" ("), style = MaterialTheme.typography.labelSmall, color = colors.textPrimary)
                            }
                        }

                        DropdownMenu(
                            expanded = deviceMenuExpanded,
                            onDismissRequest = { deviceMenuExpanded = false },
                            modifier = Modifier.background(colors.cardBackground)
                        ) {
                            DeviceType.entries.forEach { device ->
                                DropdownMenuItem(
                                    text = { Text(device.label) },
                                    onClick = {
                                        deviceMenuExpanded = false
                                        onConfigChange(config.copy(device = device))
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Theme Toggle
                    IconButton(
                        onClick = { onConfigChange(config.copy(isDarkTheme = !config.isDarkTheme)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (config.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Preview Theme",
                            tint = if (config.isDarkTheme) Color(0xFFFBBF24) else colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Refresh
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val deviceWidth = (config.device.widthDp * config.scale).dp
            val deviceHeight = (config.device.heightDp * config.scale).dp

            Surface(
                modifier = Modifier
                    .size(width = deviceWidth, height = deviceHeight)
                    .clip(RoundedCornerShape(32.dp))
                    .border(3.dp, Color(0xFF334155), RoundedCornerShape(32.dp)),
                color = if (config.isDarkTheme) Color(0xFF090D16) else Color(0xFFFFFFFF),
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Status Bar & Camera Notch
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(if (config.isDarkTheme) Color(0xFF090D16) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                    }

                    // Interactive Content (Native or Web)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (runnerType == PreviewRunnerType.WEB_JAVASCRIPT) {
                            // Acode-style Web & JavaScript Interactive Live Runner
                            val htmlPayload = if (sourceCode.contains("<html") || sourceCode.contains("<!DOCTYPE")) {
                                sourceCode
                            } else {
                                """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                                            margin: 0;
                                            padding: 16px;
                                            background: ${if (config.isDarkTheme) "#1e1f22" else "#ffffff"};
                                            color: ${if (config.isDarkTheme) "#dfe1e5" else "#1e1f22"};
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div id="app"></div>
                                    <script>
                                        try {
                                            $sourceCode
                                        } catch (e) {
                                            console.error(e);
                                        }
                                    </script>
                                </body>
                                </html>
                                """.trimIndent()
                            }

                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = WebViewClient()
                                        webChromeClient = object : WebChromeClient() {
                                            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                                message?.message()?.let { msg ->
                                                    webConsoleLogs.add("[${message.messageLevel()}] $msg")
                                                }
                                                return super.onConsoleMessage(message)
                                            }
                                        }
                                        loadDataWithBaseURL("https://localhost", htmlPayload, "text/html", "UTF-8", null)
                                    }
                                },
                                update = { webView ->
                                    webView.loadDataWithBaseURL("https://localhost", htmlPayload, "text/html", "UTF-8", null)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Native Interactive Compose Component Tree
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp)
                            ) {
                                RenderComposeNode(
                                    node = rootNode,
                                    isInspectMode = config.isInspectMode,
                                    selectedNode = selectedNode,
                                    isDark = config.isDarkTheme,
                                    onNodeClick = { node ->
                                        selectedNode = node
                                        onSelectComponent(node)
                                    }
                                )
                            }
                        }
                    }

                    // Navigation Bar Gesture Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RenderComposeNode(
    node: ComponentNode,
    isInspectMode: Boolean,
    selectedNode: ComponentNode?,
    isDark: Boolean,
    onNodeClick: (ComponentNode) -> Unit
) {
    val colors = AeroTheme.colors
    val isSelected = isInspectMode && selectedNode?.id == node.id

    Box(
        modifier = Modifier
            .then(
                if (isInspectMode) {
                    Modifier
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.primary else colors.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onNodeClick(node) }
                } else Modifier
            )
    ) {
        when (node.type) {
            "Root" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    node.children.forEach { child ->
                        RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                    }
                }
            }

            "Column" -> {
                val pad = if (node.modifier.paddingAll > 0) node.modifier.paddingAll.dp else 4.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(pad)
                ) {
                    node.children.forEach { child ->
                        RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            "Row" -> {
                val pad = if (node.modifier.paddingAll > 0) node.modifier.paddingAll.dp else 4.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(pad),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    node.children.forEach { child ->
                        RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            "Box" -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    node.children.forEach { child ->
                        RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                    }
                }
            }

            "Card" -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        node.children.forEach { child ->
                            RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                        }
                    }
                }
            }

            "Text" -> {
                val textContent = node.properties["text"] ?: "Hello AeroCode"
                val fontSize = node.properties["fontSize"]?.removeSuffix("sp")?.toIntOrNull() ?: 16
                Text(
                    text = textContent,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.padding(4.dp)
                )
            }

            "Button", "OutlinedButton", "TextButton" -> {
                val label = node.properties["label"] ?: "Button"
                val enabled = node.properties["enabled"]?.toBooleanStrictOrNull() ?: true
                var clickCount by remember { mutableIntStateOf(0) }

                if (node.type == "Button") {
                    Button(
                        onClick = { clickCount++ },
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text(
                            text = if (clickCount > 0) "$label ($clickCount)" else label,
                            color = if (colors.isDark) Color(0xFF042F2E) else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { clickCount++ },
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (clickCount > 0) "$label ($clickCount)" else label, color = colors.primary)
                    }
                }
            }

            "TextField", "OutlinedTextField" -> {
                var textVal by remember { mutableStateOf(node.properties["value"] ?: "Editable input") }
                OutlinedTextField(
                    value = textVal,
                    onValueChange = { textVal = it },
                    label = { Text(node.properties["label"] ?: "Text Field") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            "Switch" -> {
                var checked by remember { mutableStateOf(node.properties["checked"]?.toBooleanStrictOrNull() ?: true) }
                Switch(checked = checked, onCheckedChange = { checked = it })
            }

            "Checkbox" -> {
                var checked by remember { mutableStateOf(node.properties["checked"]?.toBooleanStrictOrNull() ?: true) }
                Checkbox(checked = checked, onCheckedChange = { checked = it })
            }

            "Slider" -> {
                var sliderVal by remember { mutableFloatStateOf(node.properties["value"]?.toFloatOrNull() ?: 0.5f) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Value: ${(sliderVal * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Slider(
                        value = sliderVal,
                        onValueChange = { sliderVal = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            "Spacer" -> {
                val h = node.modifier.height.takeIf { it > 0 } ?: node.properties["height"]?.toIntOrNull() ?: 0
                val w = node.modifier.width.takeIf { it > 0 } ?: node.properties["width"]?.toIntOrNull() ?: 0
                val sz = node.modifier.size.takeIf { it > 0 } ?: node.properties["size"]?.toIntOrNull() ?: 0
                val wt = node.modifier.weight.takeIf { it > 0f } ?: node.properties["weight"]?.toFloatOrNull() ?: 0f

                when {
                    sz > 0 -> Spacer(modifier = Modifier.size(sz.dp))
                    h > 0 && w > 0 -> Spacer(modifier = Modifier.size(w.dp, h.dp))
                    h > 0 -> Spacer(modifier = Modifier.height(h.dp))
                    w > 0 -> Spacer(modifier = Modifier.width(w.dp))
                    wt > 0f -> Spacer(modifier = Modifier.height(24.dp))
                    else -> Spacer(modifier = Modifier.height(16.dp))
                }
            }

            "HorizontalDivider", "Divider" -> {
                val thick = node.properties["thickness"]?.toIntOrNull() ?: 1
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    thickness = thick.dp,
                    color = if (isDark) Color(0xFF393B40) else Color(0xFFE2E8F0)
                )
            }

            "Icon" -> {
                val iconName = node.properties["icon"] ?: "Star"
                val iconVector = when (iconName.lowercase()) {
                    "favorite" -> Icons.Default.Favorite
                    "add" -> Icons.Default.Add
                    "check" -> Icons.Default.Check
                    "home" -> Icons.Default.Home
                    "search" -> Icons.Default.Search
                    "settings" -> Icons.Default.Settings
                    "share" -> Icons.Default.Share
                    "info" -> Icons.Default.Info
                    "person" -> Icons.Default.Person
                    "notifications" -> Icons.Default.Notifications
                    "image" -> Icons.Default.Image
                    else -> Icons.Default.Star
                }
                Icon(
                    imageVector = iconVector,
                    contentDescription = node.properties["contentDescription"] ?: "Icon",
                    tint = colors.primary,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                )
            }

            "Image" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF2B2D30) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = node.properties["contentDescription"] ?: "Image",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            "CircularProgressIndicator" -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp),
                    color = colors.primary
                )
            }

            "LinearProgressIndicator" -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    color = colors.primary
                )
            }

            "FilterChip", "AssistChip", "SuggestionChip", "InputChip", "Badge" -> {
                val label = node.properties["label"] ?: "Chip"
                var selected by remember { mutableStateOf(false) }
                AssistChip(
                    onClick = { selected = !selected },
                    label = { Text(label) },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            "IconButton", "FilledIconButton" -> {
                val iconName = node.properties["icon"] ?: "Star"
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = getPreviewIcon(iconName),
                        contentDescription = "Action",
                        tint = colors.primary
                    )
                }
            }

            "FloatingActionButton", "ExtendedFloatingActionButton" -> {
                val label = node.properties["label"]
                FloatingActionButton(
                    onClick = {},
                    containerColor = colors.primary,
                    contentColor = if (colors.isDark) Color(0xFF042F2E) else Color.White,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = if (label != null) 16.dp else 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        if (label != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            "NavigationBar", "BottomAppBar" -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDark) Color(0xFF2B2D30) else Color(0xFFF1F5F9),
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple("Home", Icons.Default.Home, true),
                            Triple("Search", Icons.Default.Search, false),
                            Triple("Settings", Icons.Default.Settings, false)
                        ).forEach { (title, icon, sel) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (sel) colors.primary else colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    color = if (sel) colors.primary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            "ProductCard" -> {
                val title = node.properties["title"] ?: "Product"
                val price = node.properties["price"] ?: "$99.00"
                val category = node.properties["category"] ?: "Item"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2B2D30) else Color(0xFFF8FAFC))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(category.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (isDark) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(price, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text("Buy", fontWeight = FontWeight.Bold, color = if (colors.isDark) Color(0xFF042F2E) else Color.White)
                            }
                        }
                    }
                }
            }

            "Surface" -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF2B2D30) else Color(0xFFF1F5F9)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        node.children.forEach { child ->
                            RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                        }
                    }
                }
            }

            "Scaffold" -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    node.children.forEach { child ->
                        RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                    }
                }
            }

            "TopAppBar", "CenterAlignedTopAppBar", "MediumTopAppBar", "LargeTopAppBar" -> {
                val title = node.properties["title"] ?: "App Header"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDark) Color(0xFF2B2D30) else Color(0xFFF8FAFC),
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }
            }

            "LazyColumn", "LazyRow" -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    node.children.forEach { child ->
                        RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            else -> {
                if (node.children.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        node.children.forEach { child ->
                            RenderComposeNode(child, isInspectMode, selectedNode, isDark, onNodeClick)
                        }
                    }
                } else {
                    // Graceful visual representation if node has content properties, else transparent spacer
                    val title = node.properties["title"] ?: node.properties["text"] ?: node.properties["label"]
                    if (title != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF2B2D30) else Color(0xFFF8FAFC)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    } else {
                        // Silent layout spacer
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private fun getPreviewIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name.lowercase()) {
        "favorite" -> Icons.Default.Favorite
        "add" -> Icons.Default.Add
        "check" -> Icons.Default.Check
        "home" -> Icons.Default.Home
        "search" -> Icons.Default.Search
        "settings" -> Icons.Default.Settings
        "share" -> Icons.Default.Share
        "info" -> Icons.Default.Info
        "person" -> Icons.Default.Person
        "notifications" -> Icons.Default.Notifications
        "image" -> Icons.Default.Image
        else -> Icons.Default.Star
    }
}
