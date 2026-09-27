package com.aerotech.aerocode.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.core.terminal.TerminalLine
import com.aerotech.aerocode.core.terminal.TerminalLineType
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroEmerald
import com.aerotech.aerocode.ui.theme.AeroRose
import com.aerotech.aerocode.ui.theme.DarkBorder
import com.aerotech.aerocode.ui.theme.DarkSurface
import com.aerotech.aerocode.ui.theme.DarkSurfaceVariant

@Composable
fun TerminalSheet(
    lines: List<TerminalLine>,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = DarkSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Terminal (Gradle Shell)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Row {
                    IconButton(
                        onClick = { onExecuteCommand("clear") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick task suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "./gradlew assembleDebug",
                    "./gradlew clean",
                    "./gradlew test",
                    "./gradlew dependencies",
                    "ls"
                ).forEach { suggestion ->
                    FilterChip(
                        selected = false,
                        onClick = { onExecuteCommand(suggestion) },
                        label = { Text(suggestion, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurfaceVariant,
                            labelColor = AeroCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Console output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(lines) { line ->
                    val color = when (line.type) {
                        TerminalLineType.COMMAND -> Color.White
                        TerminalLineType.SUCCESS -> AeroEmerald
                        TerminalLineType.ERROR -> AeroRose
                        TerminalLineType.INFO -> AeroCyan
                        TerminalLineType.OUTPUT -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = line.text,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = color
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Command input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$ ", style = TextStyle(fontFamily = FontFamily.Monospace, color = AeroCyan, fontWeight = FontWeight.Bold))
                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.White),
                    cursorBrush = SolidColor(AeroCyan),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onExecuteCommand(commandInput.trim())
                            commandInput = ""
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Run", tint = AeroCyan, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
