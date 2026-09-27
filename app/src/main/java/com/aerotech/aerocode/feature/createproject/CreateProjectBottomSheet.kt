package com.aerotech.aerocode.feature.createproject

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.core.project.ProjectTemplate
import com.aerotech.aerocode.core.project.ProjectTemplates
import com.aerotech.aerocode.ui.theme.AeroTheme

enum class CreateProjectStep {
    FORM,
    TEMPLATE_GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectBottomSheet(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, packageName: String, templateId: String, javaVersion: Int, gradleVersion: String, minSdk: Int) -> Unit,
    initialTemplateId: String? = null,
    initialStep: CreateProjectStep = if (initialTemplateId != null) CreateProjectStep.TEMPLATE_GRID else CreateProjectStep.FORM
) {
    val colors = AeroTheme.colors
    var currentStep by remember { mutableStateOf(initialStep) }
    val startedInTemplateGrid = remember { initialStep == CreateProjectStep.TEMPLATE_GRID }

    var projectName by remember { mutableStateOf("My Application") }
    var packageName by remember { mutableStateOf("com.example.myapplication") }
    var hasManuallyEditedPackage by remember { mutableStateOf(false) }
    var selectedTemplateId by remember {
        mutableStateOf(initialTemplateId ?: ProjectTemplates.EMPTY_COMPOSE.id)
    }

    // Java Version, Gradle Version, and SDK selection
    var selectedJavaVersion by remember { mutableIntStateOf(17) }
    var selectedGradleVersion by remember { mutableStateOf("8.7") }
    var selectedMinSdk by remember { mutableIntStateOf(26) }

    // Virtual Device / Emulator Setup
    var selectedDeviceProfile by remember { mutableStateOf("Pixel 8 Pro (API 34)") }
    var emulatorSetupMessage by remember { mutableStateOf<String?>(null) }

    // Template category filter in grid screen
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    fun updateName(name: String) {
        projectName = name
        if (!hasManuallyEditedPackage) {
            val sanitized = name.lowercase().replace("[^a-z0-9]".toRegex(), "")
            packageName = if (sanitized.isEmpty()) "com.example.app" else "com.example.$sanitized"
        }
    }

    val selectedTemplate = remember(selectedTemplateId) {
        ProjectTemplates.ALL.find { it.id == selectedTemplateId } ?: ProjectTemplates.EMPTY_COMPOSE
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
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState == CreateProjectStep.TEMPLATE_GRID) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "create_project_step_animation"
        ) { step ->
            when (step) {
                CreateProjectStep.FORM -> {
                    // Step 1: Scrollable Form with Template Button Option
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
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
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "New Project Wizard",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Android Studio Native Project Setup",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Project Name Input
                        Text(
                            text = "Application Name",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { updateName(it) },
                            placeholder = { Text("e.g. My Application") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_name"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.borderGlass,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = colors.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Package Name Input
                        Text(
                            text = "Package Identifier",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = packageName,
                            onValueChange = {
                                hasManuallyEditedPackage = true
                                packageName = it
                            },
                            placeholder = { Text("com.example.myapplication") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_package_name"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.borderGlass,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = colors.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Template Selection Section (Scrollable Form with Template button option)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Project Template",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.primary.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { currentStep = CreateProjectStep.TEMPLATE_GRID }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Browse Templates",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Selected Template Card with Live Thumbnail and Change Template Button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { currentStep = CreateProjectStep.TEMPLATE_GRID }
                                .testTag("card_selected_template"),
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Miniature thumbnail preview
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (colors.isDark) Color(0xFF141517) else Color(0xFFE2E8F0))
                                        .border(1.dp, colors.borderGlass, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TemplateMiniaturePreview(template = selectedTemplate, modifier = Modifier.fillMaxSize())
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = selectedTemplate.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (selectedTemplate.isXmlViews) Color(0xFFF97316).copy(alpha = 0.2f) else colors.primary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (selectedTemplate.isXmlViews) "XML" else "Compose",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedTemplate.isXmlViews) Color(0xFFFB923C) else colors.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = selectedTemplate.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Change Template",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = colors.borderGlass)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Build Configuration Section: Java & Gradle Versions
                        Text(
                            text = "Build & Runtime Configuration",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Java Version
                        Text(
                            text = "Java SDK Version",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(17 to "Java 17 (LTS)", 21 to "Java 21 (LTS)", 11 to "Java 11").forEach { (ver, label) ->
                                FilterChip(
                                    selected = selectedJavaVersion == ver,
                                    onClick = { selectedJavaVersion = ver },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = colors.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gradle Distribution
                        Text(
                            text = "Gradle Wrapper Distribution",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("8.7" to "Gradle 8.7", "8.5" to "Gradle 8.5", "8.2" to "Gradle 8.2").forEach { (dist, label) ->
                                FilterChip(
                                    selected = selectedGradleVersion == dist,
                                    onClick = { selectedGradleVersion = dist },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = colors.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Minimum SDK
                        Text(
                            text = "Minimum SDK",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(26 to "API 26 (Android 8.0)", 24 to "API 24 (Android 7.0)", 34 to "API 34 (Android 14)").forEach { (sdk, label) ->
                                FilterChip(
                                    selected = selectedMinSdk == sdk,
                                    onClick = { selectedMinSdk = sdk },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = colors.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = colors.borderGlass)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Virtual Device / Emulator Target Setup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Target Emulator / Virtual Device",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Pixel 8 Pro (API 34)", "Pixel 7 (API 33)", "10\" Tablet (API 34)").forEach { profile ->
                                FilterChip(
                                    selected = selectedDeviceProfile == profile,
                                    onClick = {
                                        selectedDeviceProfile = profile
                                        emulatorSetupMessage = "$profile profile assigned as default target runner"
                                    },
                                    label = { Text(profile, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = colors.primary
                                    )
                                )
                            }
                        }

                        emulatorSetupMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(msg, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
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
                                    if (projectName.isNotBlank() && packageName.isNotBlank()) {
                                        onCreateProject(
                                            projectName.trim(),
                                            packageName.trim(),
                                            selectedTemplateId,
                                            selectedJavaVersion,
                                            selectedGradleVersion,
                                            selectedMinSdk
                                        )
                                    }
                                },
                                enabled = projectName.isNotBlank() && packageName.isNotBlank(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_create_project_confirm"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                                )
                            ) {
                                Text("Finish & Setup", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                CreateProjectStep.TEMPLATE_GRID -> {
                    // Step 2: Grid Style Live Preview Template Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        // Navigation Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (startedInTemplateGrid) {
                                            onDismiss()
                                        } else {
                                            currentStep = CreateProjectStep.FORM
                                        }
                                    },
                                    modifier = Modifier.testTag("button_back_to_form")
                                ) {
                                    Icon(
                                        imageVector = if (startedInTemplateGrid) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = colors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "Project Templates",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Android Studio Native Blueprints & Live Mockups",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { currentStep = CreateProjectStep.FORM },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text("Configure", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (colors.isDark) Color(0xFF042F2E) else Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Compose", "XML Views").forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredTemplates = remember(selectedCategoryFilter) {
                            when (selectedCategoryFilter) {
                                "Compose" -> ProjectTemplates.ALL.filter { !it.isXmlViews }
                                "XML Views" -> ProjectTemplates.ALL.filter { it.isXmlViews }
                                else -> ProjectTemplates.ALL
                            }
                        }

                        // 2-Column Grid of Live Preview Template Cards
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(filteredTemplates, key = { it.id }) { template ->
                                val isSelected = template.id == selectedTemplateId
                                TemplateGridCard(
                                    template = template,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedTemplateId = template.id
                                    }
                                )
                            }
                        }

                        // Bottom Action Bar for Template Selection
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { currentStep = CreateProjectStep.FORM },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Customize in Form", color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val sanitized = selectedTemplate.name.lowercase().replace("[^a-z0-9]".toRegex(), "")
                                        val pkg = "com.example.${if (sanitized.isEmpty()) "app" else sanitized}"
                                        onCreateProject(
                                            selectedTemplate.name.substringBefore(" ("),
                                            pkg,
                                            selectedTemplateId,
                                            selectedJavaVersion,
                                            selectedGradleVersion,
                                            selectedMinSdk
                                        )
                                    },
                                    modifier = Modifier.weight(1f).testTag("button_quick_create_project"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                                ) {
                                    Text("Quick Create", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (colors.isDark) Color(0xFF042F2E) else Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grid-style card with live mockup of the template UI
 */
@Composable
fun TemplateGridCard(
    template: ProjectTemplate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.borderGlass,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag("template_card_${template.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.cardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Live Miniature Preview Device Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (colors.isDark) Color(0xFF121316) else Color(0xFFF1F3F5))
                    .border(1.dp, if (isSelected) colors.primary.copy(alpha = 0.5f) else colors.borderGlass, RoundedCornerShape(12.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                TemplateMiniaturePreview(template = template, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Template Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = template.name.substringBefore(" ("),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) colors.primary else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (colors.isDark) Color(0xFF042F2E) else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (template.isXmlViews) Color(0xFFF97316).copy(alpha = 0.2f) else colors.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (template.isXmlViews) "XML" else "Compose",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (template.isXmlViews) Color(0xFFFB923C) else colors.primary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Authentic Live Miniature Preview of the template UI
 */
@Composable
fun TemplateMiniaturePreview(
    template: ProjectTemplate,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors
    val isDark = colors.isDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) Color(0xFF1E1F22) else Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(if (isDark) Color(0xFF141517) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }

            // Body rendering specific to template
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                when (template.id) {
                    "empty_compose" -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primary.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Click", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "empty_views" -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Surface(
                                shape = RoundedCornerShape(2.dp),
                                color = Color(0xFFF97316).copy(alpha = 0.2f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text("<LinearLayout>", fontSize = 7.sp, color = Color(0xFFF97316), modifier = Modifier.padding(2.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF97316)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Button (XML)", fontSize = 7.sp, color = Color.White)
                            }
                        }
                    }

                    "bottom_navigation" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .background(colors.primary.copy(alpha = 0.2f))
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            // Bottom Nav Bar with 3 items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(if (isDark) Color(0xFF2B2D30) else Color(0xFFF1F3F5)),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = colors.primary, modifier = Modifier.size(10.dp))
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    "navigation_drawer" -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Drawer pane
                            Column(
                                modifier = Modifier
                                    .width(36.dp)
                                    .fillMaxSize()
                                    .background(colors.primary.copy(alpha = 0.2f))
                                    .padding(2.dp)
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = colors.primary, modifier = Modifier.size(8.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(colors.primary.copy(alpha = 0.5f)))
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(colors.primary.copy(alpha = 0.3f)))
                            }
                            // Main content
                            Box(modifier = Modifier.weight(1f).fillMaxSize().padding(4.dp)) {
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.Gray.copy(alpha = 0.3f)))
                            }
                        }
                    }

                    "responsive_list_detail" -> {
                        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // List pane
                            Column(modifier = Modifier.weight(0.45f).fillMaxSize()) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(14.dp)
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (it == 0) colors.primary.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f))
                                    )
                                }
                            }
                            // Detail pane
                            Box(
                                modifier = Modifier
                                    .weight(0.55f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .padding(4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(colors.primary.copy(alpha = 0.5f)))
                            }
                        }
                    }

                    "counter_app" -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Count: 7", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(16.dp).clip(CircleShape).background(colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                                Box(
                                    modifier = Modifier.size(16.dp).clip(CircleShape).background(colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }

                    "shop_ui" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDark) Color(0xFF2B2D30) else Color(0xFFF1F3F5))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Flight Pro", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    Text("$149", fontSize = 6.sp, color = colors.primary)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(14.dp)
                                        .width(24.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Buy", fontSize = 6.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDark) Color(0xFF2B2D30) else Color(0xFFF1F3F5))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Jetpack License", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    Text("$49", fontSize = 6.sp, color = colors.primary)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(14.dp)
                                        .width(24.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Buy", fontSize = 6.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
