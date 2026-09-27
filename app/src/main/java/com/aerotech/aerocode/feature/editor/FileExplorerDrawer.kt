package com.aerotech.aerocode.feature.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.core.filesystem.FileEntry
import com.aerotech.aerocode.ui.theme.AeroTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerDrawer(
    projectName: String,
    files: List<FileEntry>,
    activeFilePath: String?,
    onFileSelected: (String) -> Unit,
    onAddFile: (parentDir: String, fileName: String, initialContent: String) -> Unit,
    onDeleteFile: (path: String) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenDependencies: (() -> Unit)? = null,
    onOpenGit: (() -> Unit)? = null,
    onRenameFile: ((oldPath: String, newName: String) -> Unit)? = null,
    onDuplicateFile: ((path: String) -> Unit)? = null,
    onAddFolder: ((parentDir: String, folderName: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors
    val clipboardManager = LocalClipboardManager.current
    var showNewFileDialog by remember { mutableStateOf(false) }
    var selectedParentDir by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<String?>(null) }
    var filterMode by remember { mutableStateOf("ALL") } // "ALL", "KOTLIN", "XML"

    // Context Menu and Rename States for Long Press
    var contextMenuEntry by remember { mutableStateOf<FileEntry?>(null) }
    var renamingEntry by remember { mutableStateOf<FileEntry?>(null) }
    var renameText by remember { mutableStateOf("") }

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    // Helper to recursively collect files by extension
    fun collectFiles(entries: List<FileEntry>, ext: String): List<FileEntry> {
        val result = mutableListOf<FileEntry>()
        for (entry in entries) {
            if (!entry.isDirectory && entry.name.endsWith(ext, ignoreCase = true)) {
                result.add(entry)
            }
            if (entry.isDirectory) {
                result.addAll(collectFiles(entry.children, ext))
            }
        }
        return result
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = colors.surfaceGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header with Project Title and Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = projectName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Project Explorer",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            selectedParentDir = ""
                            showNewFileDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New file",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close drawer",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Filter Bar (All, Kotlin, XML)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterMode == "ALL",
                    onClick = { filterMode = "ALL" },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                        selectedLabelColor = colors.primary
                    )
                )

                FilterChip(
                    selected = filterMode == "KOTLIN",
                    onClick = { filterMode = "KOTLIN" },
                    label = { Text("Kotlin (.kt)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7F52FF).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFA78BFA)
                    )
                )

                FilterChip(
                    selected = filterMode == "XML",
                    onClick = { filterMode = "XML" },
                    label = { Text("XML (.xml)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF97316).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFFB923C)
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                color = colors.borderGlass
            )

            // File Tree List / Filtered View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (filterMode) {
                    "KOTLIN" -> {
                        val ktFiles = collectFiles(files, ".kt")
                        if (ktFiles.isEmpty()) {
                            Text(
                                text = "No Kotlin files found in project",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            ktFiles.forEach { file ->
                                FlatFileItem(
                                    entry = file,
                                    isActive = file.path == activeFilePath,
                                    onClick = { onFileSelected(file.path) },
                                    onLongClick = { contextMenuEntry = file },
                                    onDelete = { fileToDelete = file.path }
                                )
                            }
                        }
                    }

                    "XML" -> {
                        val xmlFiles = collectFiles(files, ".xml")
                        if (xmlFiles.isEmpty()) {
                            Text(
                                text = "No XML files found in project",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            xmlFiles.forEach { file ->
                                FlatFileItem(
                                    entry = file,
                                    isActive = file.path == activeFilePath,
                                    onClick = { onFileSelected(file.path) },
                                    onLongClick = { contextMenuEntry = file },
                                    onDelete = { fileToDelete = file.path }
                                )
                            }
                        }
                    }

                    else -> {
                        if (files.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No files found\nTap + to create Kotlin/XML file",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            files.forEach { entry ->
                                FileTreeItem(
                                    entry = entry,
                                    activeFilePath = activeFilePath,
                                    expandedFolders = expandedFolders,
                                    depth = 0,
                                    onFileClick = { file ->
                                        if (file.isDirectory) {
                                            expandedFolders[file.path] = !(expandedFolders[file.path] ?: false)
                                        } else {
                                            onFileSelected(file.path)
                                        }
                                    },
                                    onFileLongClick = { file ->
                                        contextMenuEntry = file
                                    },
                                    onAddInFolder = { folderPath ->
                                        selectedParentDir = folderPath
                                        showNewFileDialog = true
                                    },
                                    onDelete = { fileToDelete = it }
                                )
                            }
                        }
                    }
                }
            }

            // Quick Access Dock: Libraries & Git
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.cardBackground,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onOpenDependencies != null) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenDependencies() },
                            color = colors.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Extension, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Libraries", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                            }
                        }
                    }

                    if (onOpenGit != null) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenGit() },
                            color = Color(0xFFF97316).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Commit, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Git", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF97316))
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Bottom Sheet
    if (showNewFileDialog) {
        NewFileBottomSheet(
            parentPath = selectedParentDir,
            onDismiss = { showNewFileDialog = false },
            onCreateFile = { name, isDir, initialContent ->
                onAddFile(selectedParentDir, name, initialContent)
                showNewFileDialog = false
            }
        )
    }

    // Context Menu Bottom Sheet on Long Press (Android Studio style)
    contextMenuEntry?.let { entry ->
        ModalBottomSheet(
            onDismissRequest = { contextMenuEntry = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surfaceGlass
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileTypeBadge(fileName = entry.name)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = entry.path,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.borderGlass)

                // Actions list like Android Studio context menu
                if (entry.isDirectory) {
                    ContextMenuActionItem(
                        icon = Icons.Default.Add,
                        label = "New File in folder",
                        colors = colors
                    ) {
                        selectedParentDir = entry.path
                        contextMenuEntry = null
                        showNewFileDialog = true
                    }

                    ContextMenuActionItem(
                        icon = Icons.Default.CreateNewFolder,
                        label = "New Folder in directory",
                        colors = colors
                    ) {
                        selectedParentDir = entry.path
                        contextMenuEntry = null
                        newFolderName = ""
                        showNewFolderDialog = true
                    }
                } else {
                    ContextMenuActionItem(
                        icon = Icons.Default.FileOpen,
                        label = "Open in Editor",
                        colors = colors
                    ) {
                        onFileSelected(entry.path)
                        contextMenuEntry = null
                    }
                }

                ContextMenuActionItem(
                    icon = Icons.Default.DriveFileRenameOutline,
                    label = "Rename...",
                    colors = colors
                ) {
                    renamingEntry = entry
                    renameText = entry.name
                    contextMenuEntry = null
                }

                ContextMenuActionItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Relative Path",
                    colors = colors
                ) {
                    clipboardManager.setText(AnnotatedString(entry.path))
                    contextMenuEntry = null
                }

                if (!entry.isDirectory && onDuplicateFile != null) {
                    ContextMenuActionItem(
                        icon = Icons.Default.ContentCopy,
                        label = "Duplicate File",
                        colors = colors
                    ) {
                        onDuplicateFile(entry.path)
                        contextMenuEntry = null
                    }
                }

                ContextMenuActionItem(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    colors = colors,
                    isDestructive = true
                ) {
                    fileToDelete = entry.path
                    contextMenuEntry = null
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Rename Dialog
    renamingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { renamingEntry = null },
            title = {
                Text(
                    text = "Rename ${if (entry.isDirectory) "Directory" else "File"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a new name for '${entry.name}':",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderGlass,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameText.trim()
                        if (trimmed.isNotEmpty() && trimmed != entry.name) {
                            onRenameFile?.invoke(entry.path, trimmed)
                        }
                        renamingEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    enabled = renameText.isNotBlank() && renameText.trim() != entry.name
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { renamingEntry = null }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surfaceGlass
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = {
                Text(
                    text = "New Directory",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter folder name:",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("e.g. components") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderGlass,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newFolderName.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddFolder?.invoke(selectedParentDir, trimmed)
                        }
                        showNewFolderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    enabled = newFolderName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = colors.surfaceGlass
        )
    }

    // Delete Confirmation Bottom Sheet
    fileToDelete?.let { path ->
        ModalBottomSheet(
            onDismissRequest = { fileToDelete = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surfaceGlass
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Delete File?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to permanently delete:\n${path.substringAfterLast('/')}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { fileToDelete = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = colors.textPrimary)
                    }
                    Button(
                        onClick = {
                            onDeleteFile(path)
                            fileToDelete = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileTreeItem(
    entry: FileEntry,
    activeFilePath: String?,
    expandedFolders: Map<String, Boolean>,
    depth: Int,
    onFileClick: (FileEntry) -> Unit,
    onFileLongClick: (FileEntry) -> Unit,
    onAddInFolder: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val colors = AeroTheme.colors
    val isExpanded = expandedFolders[entry.path] ?: false
    val isActive = entry.path == activeFilePath

    val paddingStart = (depth * 14 + 10).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) colors.primary.copy(alpha = 0.16f) else Color.Transparent)
            .combinedClickable(
                onClick = { onFileClick(entry) },
                onLongClick = { onFileLongClick(entry) }
            )
            .padding(start = paddingStart, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.isDirectory) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(18.dp))
                FileTypeBadge(fileName = entry.name)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = if (isActive) colors.primary else colors.textPrimary,
                maxLines = 1
            )
        }

        if (entry.isDirectory) {
            IconButton(
                onClick = { onAddInFolder(entry.path) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add in folder",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            IconButton(
                onClick = { onDelete(entry.path) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    if (entry.isDirectory && isExpanded) {
        entry.children.forEach { child ->
            FileTreeItem(
                entry = child,
                activeFilePath = activeFilePath,
                expandedFolders = expandedFolders,
                depth = depth + 1,
                onFileClick = onFileClick,
                onFileLongClick = onFileLongClick,
                onAddInFolder = onAddInFolder,
                onDelete = onDelete
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FlatFileItem(
    entry: FileEntry,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AeroTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) colors.primary.copy(alpha = 0.16f) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileTypeBadge(fileName = entry.name)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    color = if (isActive) colors.primary else colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = entry.path.substringBeforeLast('/', ""),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ContextMenuActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    colors: com.aerotech.aerocode.ui.theme.AeroColors,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color(0xFFEF4444) else colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isDestructive) Color(0xFFEF4444) else colors.textPrimary
        )
    }
}

@Composable
private fun FileTypeBadge(fileName: String) {
    when {
        fileName.endsWith(".kt", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF7F52FF).copy(alpha = 0.25f)
            ) {
                Text(
                    text = "K",
                    color = Color(0xFFA78BFA),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        fileName.endsWith(".xml", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFF97316).copy(alpha = 0.25f)
            ) {
                Text(
                    text = "<>",
                    color = Color(0xFFFB923C),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
        }
        fileName.endsWith(".gradle.kts", ignoreCase = true) || fileName.endsWith(".gradle", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF02569B).copy(alpha = 0.25f)
            ) {
                Text(
                    text = "G",
                    color = Color(0xFF38BDF8),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        else -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
