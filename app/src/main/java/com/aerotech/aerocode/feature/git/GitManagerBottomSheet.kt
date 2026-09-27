package com.aerotech.aerocode.feature.git

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitManagerBottomSheet(
    project: ProjectEntity?,
    onDismiss: () -> Unit
) {
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val gitEngine = app.gitEngine
    val coroutineScope = rememberCoroutineScope()

    val gitStatus by gitEngine.status.collectAsState()
    val commits by gitEngine.commits.collectAsState()
    val branches by gitEngine.branches.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var commitMessage by remember { mutableStateOf("") }
    var newBranchName by remember { mutableStateOf("") }
    var mergeBranchName by remember { mutableStateOf("") }
    var operationNotice by remember { mutableStateOf<String?>(null) }
    var isNoticeError by remember { mutableStateOf(false) }

    val projectPath = project?.path ?: ""

    LaunchedEffect(projectPath) {
        if (projectPath.isNotBlank()) {
            gitEngine.refresh(projectPath)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceGlass,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.borderGlass)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Commit,
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Git Version Control",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = gitStatus.currentBranch,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Commit, branch, pull, push, rebase & track history",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Operation Notice if any
            operationNotice?.let { notice ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isNoticeError) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isNoticeError) Icons.Default.Close else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isNoticeError) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isNoticeError) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }
            }

            // Tabs: Status & Commit | Branches | History
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = colors.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = colors.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Changes & Sync", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Branches", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Log (${commits.size})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Quick Remote Actions Row (Pull, Push, Refresh, Rebase)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val res = gitEngine.pull(projectPath)
                                    operationNotice = res.message
                                    isNoticeError = !res.success
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.South, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pull", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val res = gitEngine.push(projectPath)
                                    operationNotice = res.message
                                    isNoticeError = !res.success
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val res = gitEngine.rebase(projectPath, "main")
                                    operationNotice = res.message
                                    isNoticeError = !res.success
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rebase", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    gitEngine.refresh(projectPath)
                                    operationNotice = "Git status refreshed"
                                    isNoticeError = false
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Working Tree Status Card
                    AeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (gitStatus.isClean) "Working directory clean" else "Uncommitted Changes",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (gitStatus.isClean) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                                Text(
                                    text = "${gitStatus.modifiedFiles.size + gitStatus.untrackedFiles.size} files",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }

                            if (gitStatus.modifiedFiles.isNotEmpty() || gitStatus.untrackedFiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column {
                                    gitStatus.modifiedFiles.take(5).forEach { file ->
                                        Text(
                                            text = "M  $file",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            color = Color(0xFF60A5FA)
                                        )
                                    }
                                    gitStatus.untrackedFiles.take(5).forEach { file ->
                                        Text(
                                            text = "?  $file",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Commit input & button
                    OutlinedTextField(
                        value = commitMessage,
                        onValueChange = { commitMessage = it },
                        placeholder = { Text("Commit message (e.g. Add XML layout & update ViewModel)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderGlass
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (commitMessage.isNotBlank()) {
                                coroutineScope.launch {
                                    val res = gitEngine.commit(projectPath, commitMessage.trim())
                                    operationNotice = res.message
                                    isNoticeError = !res.success
                                    if (res.success) {
                                        commitMessage = ""
                                    }
                                }
                            }
                        },
                        enabled = commitMessage.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                        )
                    ) {
                        Icon(Icons.Default.Commit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Commit Changes", fontWeight = FontWeight.Bold)
                    }
                }

                1 -> {
                    // Branch Management Tab
                    Column {
                        // Create New Branch input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newBranchName,
                                onValueChange = { newBranchName = it },
                                placeholder = { Text("New branch name (e.g. feature/ui)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.borderGlass
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newBranchName.isNotBlank()) {
                                        coroutineScope.launch {
                                            val res = gitEngine.createBranch(projectPath, newBranchName.trim())
                                            operationNotice = res.message
                                            isNoticeError = !res.success
                                            if (res.success) newBranchName = ""
                                        }
                                    }
                                },
                                enabled = newBranchName.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Local Branches (${branches.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, colors.borderGlass, RoundedCornerShape(14.dp)),
                            color = colors.cardBackground
                        ) {
                            LazyColumn(modifier = Modifier.padding(6.dp)) {
                                items(branches) { branch ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (!branch.isCurrent) {
                                                    coroutineScope.launch {
                                                        val res = gitEngine.checkoutBranch(projectPath, branch.name)
                                                        operationNotice = res.message
                                                        isNoticeError = !res.success
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = branch.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = if (branch.isCurrent) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (branch.isCurrent) colors.primary else colors.textPrimary
                                            )
                                            if (branch.isCurrent) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = colors.primary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "CURRENT",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = colors.primary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!branch.isCurrent) {
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            val res = gitEngine.merge(projectPath, branch.name)
                                                            operationNotice = res.message
                                                            isNoticeError = !res.success
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.CallMerge, contentDescription = "Merge into current", tint = colors.primary, modifier = Modifier.size(16.dp))
                                                }

                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            val res = gitEngine.deleteBranch(projectPath, branch.name)
                                                            operationNotice = res.message
                                                            isNoticeError = !res.success
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Version History / Commit Log Tab
                    Column {
                        Text(
                            text = "Commit History",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 320.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, colors.borderGlass, RoundedCornerShape(14.dp)),
                            color = colors.cardBackground
                        ) {
                            LazyColumn(modifier = Modifier.padding(8.dp)) {
                                items(commits) { commit ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = colors.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = commit.shortHash,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = colors.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = commit.message,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "${commit.author} • ${commit.date}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
