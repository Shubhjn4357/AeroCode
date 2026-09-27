package com.aerotech.aerocode.feature.editor

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerotech.aerocode.ui.theme.AeroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileBottomSheet(
    parentPath: String,
    onDismiss: () -> Unit,
    onCreateFile: (name: String, isDirectory: Boolean, initialContent: String) -> Unit
) {
    val colors = AeroTheme.colors
    var name by remember { mutableStateOf("") }
    var selectedFileType by remember { mutableStateOf("KOTLIN") } // KOTLIN, XML_LAYOUT, XML_VALUES, FOLDER, OTHER

    val isDirectory = selectedFileType == "FOLDER"

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
        ) {
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
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDirectory) Icons.Default.CreateNewFolder else Icons.Default.NoteAdd,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isDirectory) "Create Directory" else "Create File",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (parentPath.isNotBlank()) parentPath.substringAfterLast('/') else "Project Root",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type selector chips: Kotlin, XML Layout, XML Values, Folder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFileType == "KOTLIN",
                    onClick = {
                        selectedFileType = "KOTLIN"
                        if (name.isBlank() || name.endsWith(".xml")) name = "NewComponent.kt"
                    },
                    label = { Text("Kotlin (.kt)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF818CF8).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFF818CF8)
                    )
                )
                FilterChip(
                    selected = selectedFileType == "XML_LAYOUT",
                    onClick = {
                        selectedFileType = "XML_LAYOUT"
                        if (name.isBlank() || name.endsWith(".kt")) name = "activity_custom.xml"
                    },
                    label = { Text("XML Layout") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF97316).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFFF97316)
                    )
                )
                FilterChip(
                    selected = selectedFileType == "FOLDER",
                    onClick = {
                        selectedFileType = "FOLDER"
                        name = ""
                    },
                    label = { Text("Folder") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                        selectedLabelColor = colors.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        when (selectedFileType) {
                            "FOLDER" -> "Folder Name"
                            "KOTLIN" -> "Kotlin File Name (e.g. MyScreen.kt)"
                            "XML_LAYOUT" -> "XML Layout Name (e.g. view_card.xml)"
                            else -> "File Name"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.borderGlass,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val cleanName = name.trim()
                            val initialContent = when (selectedFileType) {
                                "KOTLIN" -> """
                                    package com.aerotech.aerocode
                                    
                                    import androidx.compose.runtime.Composable
                                    import androidx.compose.material3.Text
                                    import androidx.compose.foundation.layout.Box
                                    import androidx.compose.ui.Modifier
                                    
                                    @Composable
                                    fun ${cleanName.substringBeforeLast(".kt").replaceFirstChar { it.uppercase() }}() {
                                        Box {
                                            Text(text = "Hello from $cleanName")
                                        }
                                    }
                                """.trimIndent()
                                "XML_LAYOUT" -> """
                                    <?xml version="1.0" encoding="utf-8"?>
                                    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                        android:layout_width="match_parent"
                                        android:layout_height="match_parent"
                                        android:orientation="vertical"
                                        android:padding="16dp">
                                    
                                        <TextView
                                            android:id="@+id/title_text"
                                            android:layout_width="wrap_content"
                                            android:layout_height="wrap_content"
                                            android:text="Welcome to AeroCode"
                                            android:textSize="18sp" />
                                    
                                    </LinearLayout>
                                """.trimIndent()
                                else -> "// Created with AeroCode\n"
                            }
                            onCreateFile(cleanName, isDirectory, initialContent)
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
