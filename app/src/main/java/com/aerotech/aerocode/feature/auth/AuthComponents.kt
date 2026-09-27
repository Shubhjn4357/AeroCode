package com.aerotech.aerocode.feature.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.data.firebase.CloudProject
import com.aerotech.aerocode.data.firebase.UserProfile
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserAccountBadge(
    user: UserProfile?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = colors.surfaceGlass,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (user != null) colors.primary.copy(alpha = 0.2f) else colors.textSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (user != null) {
                    val initial = user.displayName?.firstOrNull()?.uppercase() ?: user.email?.firstOrNull()?.uppercase() ?: "U"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sign In",
                        modifier = Modifier.size(18.dp),
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = user?.displayName ?: "Sign In",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = if (user != null) colors.textPrimary else colors.primary
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Sync dot indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (user != null) Color(0xFF34D399) else Color(0xFFF59E0B))
            )
        }
    }
}

@Composable
fun CloudSyncHomeCard(
    user: UserProfile?,
    projects: List<ProjectEntity>,
    onOpenAccountDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors
    val app = AeroCodeApplication.instance
    val syncStatus by app.firestoreSyncManager.syncStatus.collectAsState()
    val scope = rememberCoroutineScope()

    AeroGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = if (syncStatus.isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Firestore Cloud Sync",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (user != null) "Connected as ${user.email ?: user.displayName}" else "Sign in to backup projects across devices",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (user != null) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                // Sync all current projects to Firestore
                                for (proj in projects) {
                                    val files = mutableMapOf<String, String>()
                                    try {
                                        val mainFilePath = app.projectRepository.getMainFilePath(proj.id)
                                        if (mainFilePath != null) {
                                            files["MainActivity.kt"] = app.fileSystem.read(mainFilePath)
                                        }
                                    } catch (_: Exception) {}
                                    app.firestoreSyncManager.syncProjectToCloud(proj, files)
                                }
                            }
                        },
                        enabled = !syncStatus.isSyncing
                    ) {
                        if (syncStatus.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now", tint = colors.primary)
                        }
                    }
                } else {
                    Button(
                        onClick = onOpenAccountDialog,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Connect", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }
                }
            }

            if (syncStatus.message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (syncStatus.isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (syncStatus.isError) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = syncStatus.message ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (syncStatus.isError) Color(0xFFEF4444) else colors.textSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSyncDialog(
    onDismiss: () -> Unit,
    projects: List<ProjectEntity> = emptyList(),
    onRestoreProject: (CloudProject) -> Unit = {}
) {
    val context = LocalContext.current
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val user by app.authService.currentUser.collectAsState()
    val cloudProjects by app.firestoreSyncManager.cloudProjects.collectAsState()
    val syncStatus by app.firestoreSyncManager.syncStatus.collectAsState()
    val scope = rememberCoroutineScope()

    var isEmailAuthMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (user != null) "AeroCode Cloud Profile" else "Sign In to AeroCode",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                if (user != null) {
                    // Profile Overview
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceGlass),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = user?.displayName?.firstOrNull()?.uppercase() ?: user?.email?.firstOrNull()?.uppercase() ?: "U"
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user?.displayName ?: "Signed In User",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = user?.email ?: if (user?.isAnonymous == true) "Guest Session" else "No email",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = colors.borderGlass)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Firestore Status:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "Connected (Live)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cloud Project Backups (${cloudProjects.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (cloudProjects.isEmpty()) {
                        Text(
                            text = "No projects synced to Firestore yet. Tap 'Sync All Projects' below to backup your work.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            items(cloudProjects, key = { it.id }) { proj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceGlass)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = proj.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = proj.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRestoreProject(proj) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = "Restore",
                                            tint = colors.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Sync All Projects
                    Button(
                        onClick = {
                            scope.launch {
                                for (proj in projects) {
                                    val files = mutableMapOf<String, String>()
                                    try {
                                        val mainFilePath = app.projectRepository.getMainFilePath(proj.id)
                                        if (mainFilePath != null) {
                                            files["MainActivity.kt"] = app.fileSystem.read(mainFilePath)
                                        }
                                    } catch (_: Exception) {}
                                    app.firestoreSyncManager.syncProjectToCloud(proj, files)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync All Local Projects to Cloud", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            app.authService.signOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = colors.textSecondary)
                    }

                } else {
                    // Not signed in state
                    Text(
                        text = "Sign in to securely identify your developer profile and automatically sync your Jetpack Compose projects with Firestore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!isEmailAuthMode) {
                        // Google Sign-In Button
                        Button(
                            onClick = {
                                isLoading = true
                                authError = null
                                scope.launch {
                                    val result = app.authService.signInWithGoogle(context)
                                    isLoading = false
                                    result.onSuccess { profile ->
                                        app.firestoreSyncManager.saveUserProfile(profile)
                                        app.firestoreSyncManager.fetchCloudProjects()
                                    }.onFailure { err ->
                                        // Attempt guest fallback if Google Play Services is unavailable
                                        val guestResult = app.authService.signInAnonymously("Developer")
                                        guestResult.onSuccess { profile ->
                                            app.firestoreSyncManager.saveUserProfile(profile)
                                        }.onFailure {
                                            authError = "Sign in: ${err.localizedMessage}"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign in with Google", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Continue as Developer Guest
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                authError = null
                                scope.launch {
                                    val result = app.authService.signInAnonymously("Developer Guest")
                                    isLoading = false
                                    result.onSuccess { profile ->
                                        app.firestoreSyncManager.saveUserProfile(profile)
                                    }.onFailure { err ->
                                        authError = err.localizedMessage
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            Text("Continue as Guest / Anonymous", color = colors.textPrimary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = { isEmailAuthMode = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Use Email & Password", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                        }
                    } else {
                        // Email/Password mode
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    isLoading = true
                                    authError = null
                                    scope.launch {
                                        val result = app.authService.signInWithEmail(emailInput, passwordInput)
                                        isLoading = false
                                        result.onSuccess { profile ->
                                            app.firestoreSyncManager.saveUserProfile(profile)
                                        }.onFailure { err ->
                                            authError = err.localizedMessage
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                enabled = !isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank()
                            ) {
                                Text("Sign In", color = Color.Black)
                            }
                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    authError = null
                                    scope.launch {
                                        val result = app.authService.signUpWithEmail(emailInput, passwordInput)
                                        isLoading = false
                                        result.onSuccess { profile ->
                                            app.firestoreSyncManager.saveUserProfile(profile)
                                        }.onFailure { err ->
                                            authError = err.localizedMessage
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank()
                            ) {
                                Text("Sign Up", color = colors.textPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { isEmailAuthMode = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Back to Google Sign-In", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
