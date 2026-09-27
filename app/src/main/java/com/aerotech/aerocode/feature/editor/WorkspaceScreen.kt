package com.aerotech.aerocode.feature.editor

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Terminal
import com.aerotech.aerocode.feature.adb.AdbToolsBottomSheet
import com.aerotech.aerocode.feature.dependencies.DependencyManagerBottomSheet
import com.aerotech.aerocode.feature.editor.WorkspaceMenuBottomSheet
import com.aerotech.aerocode.feature.git.GitManagerBottomSheet
import com.aerotech.aerocode.feature.gradle.GradleManagerBottomSheet
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.core.compiler.ComposeASTParser
import com.aerotech.aerocode.core.compiler.ComposeCodeGenerator
import com.aerotech.aerocode.core.filesystem.FileEntry
import com.aerotech.aerocode.core.storage.BuildSettings
import com.aerotech.aerocode.core.storage.EditorSettings
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.domain.build.BuildState
import com.aerotech.aerocode.domain.editor.CompletionItem
import com.aerotech.aerocode.domain.editor.Diagnostic
import com.aerotech.aerocode.domain.editor.Document
import com.aerotech.aerocode.domain.editor.Position
import com.aerotech.aerocode.domain.preview.PreviewConfig
import com.aerotech.aerocode.domain.visual.ComponentCatalog
import com.aerotech.aerocode.domain.visual.ComponentNode
import com.aerotech.aerocode.domain.visual.ComposeComponentDefinition
import com.aerotech.aerocode.feature.build.BuildOutputSheet
import com.aerotech.aerocode.feature.preview.PreviewScreen
import com.aerotech.aerocode.feature.terminal.TerminalSheet
import com.aerotech.aerocode.feature.visual.VisualEditorScreen
import com.aerotech.aerocode.ui.theme.AeroAmber
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroTheme
import com.aerotech.aerocode.ui.theme.DarkBg
import com.aerotech.aerocode.ui.theme.DarkBorder
import com.aerotech.aerocode.ui.theme.DarkSurface
import com.aerotech.aerocode.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class EditorMode {
    CODE,
    PREVIEW,
    VISUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val coroutineScope = rememberCoroutineScope()

    var project by remember { mutableStateOf<ProjectEntity?>(null) }
    var fileList by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var activeFilePath by remember { mutableStateOf<String?>(null) }
    val openFiles = remember { mutableStateListOf<String>() }

    var editorMode by remember { mutableStateOf(EditorMode.CODE) }
    var sourceCode by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) }

    // History undo/redo
    val undoStack = remember { mutableListOf<String>() }
    val redoStack = remember { mutableListOf<String>() }

    // Diagnostics & Completions
    var diagnostics by remember { mutableStateOf<List<Diagnostic>>(emptyList()) }
    var completions by remember { mutableStateOf<List<CompletionItem>>(emptyList()) }

    // Visual AST
    var rootNode by remember { mutableStateOf(ComponentNode(id = "root", type = "Root")) }
    var selectedVisualNode by remember { mutableStateOf<ComponentNode?>(null) }

    // Preview
    var previewConfig by remember { mutableStateOf(PreviewConfig()) }

    // Drawers and Sheets
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showMenuSheet by remember { mutableStateOf(false) }
    var showGitSheet by remember { mutableStateOf(false) }
    var showDependenciesSheet by remember { mutableStateOf(false) }
    var showBuildSheet by remember { mutableStateOf(false) }
    var showTerminalSheet by remember { mutableStateOf(false) }
    var showAdbSheet by remember { mutableStateOf(false) }
    var showGradleSheet by remember { mutableStateOf(false) }

    val buildState by app.buildEngine.buildState.collectAsState(initial = BuildState.Idle)
    val terminalLines by app.terminalEngine.lines.collectAsState()
    val editorSettings by app.preferences.editorSettings.collectAsState(initial = EditorSettings())
    val buildSettings by app.preferences.buildSettings.collectAsState(initial = BuildSettings())

    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (editorMode != EditorMode.CODE) {
            editorMode = EditorMode.CODE
        } else {
            onNavigateBack()
        }
    }

    // Load Project & Files on start
    LaunchedEffect(projectId) {
        val p = app.projectRepository.getProject(projectId)
        project = p
        if (p != null) {
            fileList = app.fileSystem.list(p.path)
            // Auto-index the entire repository for dynamic suggestions & custom composables
            app.repoSymbolIndexer.indexProject(p.path, app.fileSystem)

            val ws = app.projectRepository.getWorkspace(projectId)
            val mainFile = ws?.activeFilePath ?: "${p.path}/app/src/main/kotlin/${p.packageName.replace('.', '/')}/MainActivity.kt"
            activeFilePath = mainFile
            if (!openFiles.contains(mainFile)) openFiles.add(mainFile)

            if (app.fileSystem.exists(mainFile)) {
                val content = app.fileSystem.read(mainFile)
                sourceCode = content
                rootNode = ComposeASTParser.parse(content, app.repoSymbolIndexer.getAllComposableAsts())
            }
        }
    }

    // Refresh file list helper
    fun refreshFiles() {
        val p = project ?: return
        coroutineScope.launch {
            fileList = app.fileSystem.list(p.path)
            app.repoSymbolIndexer.indexProject(p.path, app.fileSystem)
        }
    }

    // Open file helper
    fun switchActiveFile(path: String) {
        coroutineScope.launch {
            if (!openFiles.contains(path)) {
                openFiles.add(path)
            }
            activeFilePath = path
            if (app.fileSystem.exists(path)) {
                val content = app.fileSystem.read(path)
                sourceCode = content
                undoStack.clear()
                redoStack.clear()
                isDirty = false
                rootNode = ComposeASTParser.parse(content, app.repoSymbolIndexer.getAllComposableAsts())
                drawerState.close()
            }
        }
    }

    // Autosave debouncer
    var autoSaveJob by remember { mutableStateOf<Job?>(null) }
    fun onCodeUpdated(newText: String) {
        if (sourceCode != newText) {
            undoStack.add(sourceCode)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            sourceCode = newText
            isDirty = true

            // Instantly update dynamic repository index for this active file
            activeFilePath?.let { app.repoSymbolIndexer.updateFile(it, newText) }

            // Trigger AST parse for preview & visual with all indexed composable definitions
            rootNode = ComposeASTParser.parse(newText, app.repoSymbolIndexer.getAllComposableAsts())

            // Update diagnostics off main thread
            coroutineScope.launch {
                val doc = Document(activeFilePath ?: "", newText)
                diagnostics = app.languageService.diagnostics(doc)
            }

            // Autosave after 800ms
            autoSaveJob?.cancel()
            autoSaveJob = coroutineScope.launch {
                delay(800)
                activeFilePath?.let { path ->
                    app.fileSystem.write(path, newText)
                    isDirty = false
                }
            }
        }
    }

    // Manual Save
    fun saveFile() {
        activeFilePath?.let { path ->
            coroutineScope.launch {
                app.fileSystem.write(path, sourceCode)
                isDirty = false
            }
        }
    }

    // AST Mutation callback from Visual Editor
    fun onASTMutated(newRoot: ComponentNode) {
        rootNode = newRoot
        val updatedCode = ComposeCodeGenerator.updateSourceWithAST(sourceCode, newRoot)
        onCodeUpdated(updatedCode)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FileExplorerDrawer(
                projectName = project?.name ?: "AeroCode Project",
                files = fileList,
                activeFilePath = activeFilePath,
                onFileSelected = { switchActiveFile(it) },
                onAddFile = { parentDir, name, initialContent ->
                    coroutineScope.launch {
                        val path = if (parentDir.isNotBlank()) "$parentDir/$name" else "${project?.path}/$name"
                        app.fileSystem.write(path, initialContent)
                        refreshFiles()
                        switchActiveFile(path)
                    }
                },
                onDeleteFile = { path ->
                    coroutineScope.launch {
                        app.fileSystem.delete(path)
                        openFiles.remove(path)
                        if (activeFilePath == path) {
                            val next = openFiles.firstOrNull()
                            if (next != null) {
                                switchActiveFile(next)
                            } else {
                                activeFilePath = null
                                sourceCode = ""
                            }
                        }
                        refreshFiles()
                    }
                },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                onOpenDependencies = { showDependenciesSheet = true },
                onOpenGit = { showGitSheet = true },
                onRenameFile = { oldPath, newName ->
                    coroutineScope.launch {
                        val parent = oldPath.substringBeforeLast('/', "")
                        val newPath = if (parent.isNotEmpty()) "$parent/$newName" else newName
                        app.fileSystem.rename(oldPath, newPath)
                        val idx = openFiles.indexOf(oldPath)
                        if (idx != -1) {
                            openFiles[idx] = newPath
                        }
                        if (activeFilePath == oldPath) {
                            activeFilePath = newPath
                        }
                        refreshFiles()
                    }
                },
                onDuplicateFile = { path ->
                    coroutineScope.launch {
                        val ext = path.substringAfterLast('.', "")
                        val base = path.substringBeforeLast('.')
                        val newPath = if (ext.isNotEmpty()) "${base}_copy.$ext" else "${path}_copy"
                        app.fileSystem.copy(path, newPath)
                        refreshFiles()
                    }
                },
                onAddFolder = { parentDir, folderName ->
                    coroutineScope.launch {
                        val folderPath = if (parentDir.isNotBlank()) "$parentDir/$folderName" else "${project?.path}/$folderName"
                        app.fileSystem.createDirectory(folderPath)
                        refreshFiles()
                    }
                }
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
        ) {
            // Compact Workspace Top Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surfaceGlass,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back button, Folder Drawer opener, Project & File Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f))
                                .clickable { coroutineScope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "Files", tint = colors.primary, modifier = Modifier.size(18.dp))
                        }

                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = project?.name ?: "Workspace",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val currentName = activeFilePath?.substringAfterLast('/') ?: "No file open"
                                Text(
                                    text = currentName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = colors.textSecondary,
                                    maxLines = 1
                                )
                                if (isDirty) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                }
                            }
                        }
                    }

                    // Compact Right: Run / Build button + Overflow Menu Model Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Run / Build APK button (Android Studio Green style)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    saveFile()
                                    showBuildSheet = true
                                    project?.let { p ->
                                        coroutineScope.launch {
                                            if (buildSettings.buildVariant == "release") {
                                                app.buildEngine.assembleRelease(p)
                                            } else {
                                                app.buildEngine.assembleDebug(p)
                                            }
                                        }
                                    }
                                }
                                .testTag("workspace_button_build")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Run",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // More Actions Menu Button (Opens Menu Bottom Sheet for Git, Gradle, Libraries, ADB, Terminal, Save, Cloud)
                        IconButton(
                            onClick = { showMenuSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Open Files Tabs Row
            if (openFiles.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.surfaceGlass,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.borderGlass)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        openFiles.forEach { path ->
                            val isSelected = path == activeFilePath
                            val name = path.substringAfterLast('/')
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)) else null,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .clickable { switchActiveFile(path) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) colors.primary else colors.textSecondary
                                    )
                                    if (openFiles.size > 1) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close tab",
                                            tint = colors.textSecondary,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable {
                                                    openFiles.remove(path)
                                                    if (activeFilePath == path) {
                                                        openFiles.firstOrNull()?.let { switchActiveFile(it) }
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Triad Mode Switcher: CODE | PREVIEW | VISUAL
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surfaceGlass,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.borderGlass)
            ) {
                TabRow(
                    selectedTabIndex = editorMode.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = colors.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[editorMode.ordinal]),
                            color = colors.primary
                        )
                    }
                ) {
                    Tab(
                        selected = editorMode == EditorMode.CODE,
                        onClick = { editorMode = EditorMode.CODE },
                        text = {
                            Text(
                                "CODE",
                                fontWeight = if (editorMode == EditorMode.CODE) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (editorMode == EditorMode.CODE) colors.primary else colors.textSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_mode_code")
                    )
                    Tab(
                        selected = editorMode == EditorMode.PREVIEW,
                        onClick = {
                            saveFile()
                            rootNode = ComposeASTParser.parse(sourceCode, app.repoSymbolIndexer.getAllComposableAsts())
                            editorMode = EditorMode.PREVIEW
                        },
                        text = {
                            Text(
                                "PREVIEW",
                                fontWeight = if (editorMode == EditorMode.PREVIEW) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (editorMode == EditorMode.PREVIEW) colors.primary else colors.textSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_mode_preview")
                    )
                    Tab(
                        selected = editorMode == EditorMode.VISUAL,
                        onClick = {
                            saveFile()
                            rootNode = ComposeASTParser.parse(sourceCode, app.repoSymbolIndexer.getAllComposableAsts())
                            editorMode = EditorMode.VISUAL
                        },
                        text = {
                            Text(
                                "VISUAL",
                                fontWeight = if (editorMode == EditorMode.VISUAL) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (editorMode == EditorMode.VISUAL) colors.primary else colors.textSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_mode_visual")
                    )
                }
            }

            // Mode Content Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (editorMode) {
                    EditorMode.CODE -> {
                        CodeEditorView(
                            initialContent = sourceCode,
                            onContentChange = { onCodeUpdated(it) },
                            diagnostics = diagnostics,
                            completions = completions,
                            activeFilePath = activeFilePath ?: "",
                            fontSizeSp = editorSettings.fontSizeSp,
                            showLineNumbers = editorSettings.showLineNumbers,
                            tabSize = editorSettings.tabSize,
                            onCursorChange = { line, col, currentText ->
                                coroutineScope.launch {
                                    val doc = Document(activeFilePath ?: "", currentText)
                                    completions = app.languageService.completions(doc, Position(line, col))
                                }
                            },
                            onSelectCompletion = { comp ->
                                comp.autoImport?.let { imp ->
                                    if (!sourceCode.contains(imp) && !(activeFilePath?.endsWith(".xml") ?: false)) {
                                        val lines = sourceCode.lines().toMutableList()
                                        val insertIdx = lines.indexOfLast { it.startsWith("import ") }
                                            .coerceAtLeast(0) + 1
                                        lines.add(insertIdx, "import $imp")
                                        val newCode = lines.joinToString("\n")
                                        onCodeUpdated(newCode)
                                    }
                                }
                            }
                        )
                    }

                    EditorMode.PREVIEW -> {
                        PreviewScreen(
                            rootNode = rootNode,
                            config = previewConfig,
                            onConfigChange = { previewConfig = it },
                            onSelectComponent = { node ->
                                selectedVisualNode = node
                            },
                            onSwitchToVisual = {
                                editorMode = EditorMode.VISUAL
                            },
                            onSwitchToCode = { line ->
                                editorMode = EditorMode.CODE
                            },
                            onRefresh = {
                                rootNode = ComposeASTParser.parse(sourceCode, app.repoSymbolIndexer.getAllComposableAsts())
                            },
                            sourceCode = sourceCode,
                            activeFilePath = activeFilePath ?: ""
                        )
                    }

                    EditorMode.VISUAL -> {
                        VisualEditorScreen(
                            rootNode = rootNode,
                            selectedNode = selectedVisualNode,
                            onSelectNode = { selectedVisualNode = it },
                            onMutateNode = { mutated ->
                                fun updateTree(node: ComponentNode): ComponentNode {
                                    if (node.id == mutated.id) return mutated
                                    return node.copy(children = node.children.map { updateTree(it) }.toMutableList())
                                }
                                val newRoot = updateTree(rootNode)
                                onASTMutated(newRoot)
                            },
                            onInsertChild = { parent, def ->
                                val newNode = ComponentNode(
                                    id = UUID.randomUUID().toString(),
                                    type = def.name,
                                    label = def.name,
                                    parentId = parent.id
                                )
                                parent.children.add(newNode)
                                onASTMutated(rootNode)
                            },
                            onDeleteNode = { target ->
                                fun removeNode(node: ComponentNode) {
                                    node.children.removeAll { it.id == target.id }
                                    node.children.forEach { removeNode(it) }
                                }
                                removeNode(rootNode)
                                selectedVisualNode = null
                                onASTMutated(rootNode)
                            },
                            onMoveNodeUp = { target ->
                                fun reorder(node: ComponentNode) {
                                    val idx = node.children.indexOfFirst { it.id == target.id }
                                    if (idx > 0) {
                                        val item = node.children.removeAt(idx)
                                        node.children.add(idx - 1, item)
                                    } else {
                                        node.children.forEach { reorder(it) }
                                    }
                                }
                                reorder(rootNode)
                                onASTMutated(rootNode)
                            },
                            onMoveNodeDown = { target ->
                                fun reorder(node: ComponentNode) {
                                    val idx = node.children.indexOfFirst { it.id == target.id }
                                    if (idx != -1 && idx < node.children.size - 1) {
                                        val item = node.children.removeAt(idx)
                                        node.children.add(idx + 1, item)
                                    } else {
                                        node.children.forEach { reorder(it) }
                                    }
                                }
                                reorder(rootNode)
                                onASTMutated(rootNode)
                            },
                            onViewInCode = { editorMode = EditorMode.CODE }
                        )
                    }
                }
            }
        }
    }

    // Build Output Bottom Sheet
    if (showBuildSheet) {
        BuildOutputSheet(
            projectName = project?.name ?: "App",
            buildState = buildState,
            onDismiss = { showBuildSheet = false },
            onInstallApk = { apkPath ->
                coroutineScope.launch {
                    app.adbTools.installApk(apkPath)
                }
            }
        )
    }

    // Terminal Modal Bottom Sheet
    if (showTerminalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTerminalSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surfaceGlass
        ) {
            TerminalSheet(
                lines = terminalLines,
                onExecuteCommand = { cmd ->
                    coroutineScope.launch {
                        app.terminalEngine.execute(cmd, project)
                    }
                },
                onDismiss = { showTerminalSheet = false }
            )
        }
    }

    // ADB Platform Tools Bottom Sheet
    if (showAdbSheet) {
        AdbToolsBottomSheet(
            project = project,
            onDismiss = { showAdbSheet = false }
        )
    }

    // Gradle Distribution Manager Bottom Sheet
    if (showGradleSheet) {
        GradleManagerBottomSheet(
            project = project,
            onDismiss = { showGradleSheet = false }
        )
    }

    // Workspace Actions & Tools Menu Modal Bottom Sheet
    if (showMenuSheet) {
        WorkspaceMenuBottomSheet(
            onDismiss = { showMenuSheet = false },
            onSaveFile = { saveFile() },
            onOpenGit = { showGitSheet = true },
            onOpenGradle = { showGradleSheet = true },
            onOpenDependencies = { showDependenciesSheet = true },
            onOpenTerminal = { showTerminalSheet = true },
            onOpenAdb = { showAdbSheet = true },
            onSyncCloud = {
                saveFile()
                project?.let { p ->
                    coroutineScope.launch {
                        val files = mutableMapOf<String, String>()
                        activeFilePath?.let { path ->
                            files[path.substringAfterLast('/')] = sourceCode
                        }
                        app.firestoreSyncManager.syncProjectToCloud(p, files)
                    }
                }
            },
            onTriggerBuild = {
                saveFile()
                showBuildSheet = true
                project?.let { p ->
                    coroutineScope.launch {
                        if (buildSettings.buildVariant == "release") {
                            app.buildEngine.assembleRelease(p)
                        } else {
                            app.buildEngine.assembleDebug(p)
                        }
                    }
                }
            }
        )
    }

    // Git Version Control Bottom Sheet
    if (showGitSheet) {
        GitManagerBottomSheet(
            project = project,
            onDismiss = { showGitSheet = false }
        )
    }

    // Android Studio Dependencies & Extensions Manager Bottom Sheet
    if (showDependenciesSheet) {
        DependencyManagerBottomSheet(
            project = project,
            onDismiss = { showDependenciesSheet = false }
        )
    }
}
