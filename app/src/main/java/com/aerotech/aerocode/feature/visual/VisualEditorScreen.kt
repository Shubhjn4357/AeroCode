package com.aerotech.aerocode.feature.visual

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.aerotech.aerocode.ui.theme.AeroTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.domain.visual.ComponentCatalog
import com.aerotech.aerocode.domain.visual.ComponentNode
import com.aerotech.aerocode.domain.visual.ComposeComponentDefinition
import com.aerotech.aerocode.domain.visual.ModifierModel
import com.aerotech.aerocode.domain.visual.PropertyDefinition
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroIndigo
import com.aerotech.aerocode.ui.theme.AeroRose
import com.aerotech.aerocode.ui.theme.DarkBg
import com.aerotech.aerocode.ui.theme.DarkBorder
import com.aerotech.aerocode.ui.theme.DarkSurface
import com.aerotech.aerocode.ui.theme.DarkSurfaceVariant
import java.util.UUID

@Composable
fun VisualEditorScreen(
    rootNode: ComponentNode,
    selectedNode: ComponentNode?,
    onSelectNode: (ComponentNode) -> Unit,
    onMutateNode: (ComponentNode) -> Unit,
    onInsertChild: (parent: ComponentNode, definition: ComposeComponentDefinition) -> Unit,
    onDeleteNode: (ComponentNode) -> Unit,
    onMoveNodeUp: (ComponentNode) -> Unit,
    onMoveNodeDown: (ComponentNode) -> Unit,
    onViewInCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showInsertSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tree & Layout, 1: Properties, 2: Modifiers

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Visual Header with Tabs: Tree, Properties, Modifiers
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = AeroCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Visual Compose Designer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Row {
                        Button(
                            onClick = { showInsertSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AeroCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF042F2E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Insert", color = Color(0xFF042F2E), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = onViewInCode, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Code, contentDescription = "View Code", tint = AeroCyan)
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
                    contentColor = AeroCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AeroCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Hierarchy Tree", fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Properties", fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Modifiers", fontSize = 13.sp) }
                    )
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Visual Tree
                    HierarchyTreeView(
                        rootNode = rootNode,
                        selectedNode = selectedNode,
                        onSelectNode = onSelectNode,
                        onMoveUp = onMoveNodeUp,
                        onMoveDown = onMoveNodeDown,
                        onDelete = onDeleteNode
                    )
                }

                1 -> {
                    // Properties Inspector
                    if (selectedNode != null) {
                        PropertyInspectorView(
                            node = selectedNode,
                            onUpdateNode = onMutateNode
                        )
                    } else {
                        EmptySelectionMessage("Select a component in the tree to edit its properties.")
                    }
                }

                2 -> {
                    // Modifier Editor
                    if (selectedNode != null) {
                        ModifierEditorView(
                            node = selectedNode,
                            onUpdateModifier = { mod ->
                                onMutateNode(selectedNode.copy(modifier = mod))
                            }
                        )
                    } else {
                        EmptySelectionMessage("Select a component in the tree to edit its modifiers.")
                    }
                }
            }
        }
    }

    // Component Insertion Sheet
    if (showInsertSheet) {
        ComponentInsertDialog(
            targetNode = selectedNode ?: rootNode,
            onDismiss = { showInsertSheet = false },
            onSelectComponent = { definition ->
                showInsertSheet = false
                val parent = if (selectedNode != null && isContainer(selectedNode.type)) selectedNode else rootNode
                onInsertChild(parent, definition)
            }
        )
    }
}

@Composable
private fun EmptySelectionMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HierarchyTreeView(
    rootNode: ComponentNode,
    selectedNode: ComponentNode?,
    onSelectNode: (ComponentNode) -> Unit,
    onMoveUp: (ComponentNode) -> Unit,
    onMoveDown: (ComponentNode) -> Unit,
    onDelete: (ComponentNode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TreeItemNode(
            node = rootNode,
            level = 0,
            selectedNode = selectedNode,
            onSelectNode = onSelectNode,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            onDelete = onDelete
        )
    }
}

@Composable
fun TreeItemNode(
    node: ComponentNode,
    level: Int,
    selectedNode: ComponentNode?,
    onSelectNode: (ComponentNode) -> Unit,
    onMoveUp: (ComponentNode) -> Unit,
    onMoveDown: (ComponentNode) -> Unit,
    onDelete: (ComponentNode) -> Unit
) {
    val isSelected = selectedNode?.id == node.id

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AeroCyan else DarkBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelectNode(node) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isContainer(node.type)) AeroIndigo.copy(alpha = 0.2f) else AeroCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = node.type.take(1),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isContainer(node.type)) AeroIndigo else AeroCyan
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.type,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) AeroCyan else Color.White
                )
                if (node.properties.isNotEmpty()) {
                    val summary = node.properties.entries.take(1).joinToString { "${it.key}: ${it.value}" }
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (node.type != "Root") {
                IconButton(onClick = { onMoveUp(node) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { onMoveDown(node) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { onDelete(node) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AeroRose, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    node.children.forEach { child ->
        TreeItemNode(
            node = child,
            level = level + 1,
            selectedNode = selectedNode,
            onSelectNode = onSelectNode,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            onDelete = onDelete
        )
    }
}

@Composable
fun PropertyInspectorView(
    node: ComponentNode,
    onUpdateNode: (ComponentNode) -> Unit
) {
    val definition = ComponentCatalog.findDefinition(node.type)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${node.type} Properties",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AeroCyan
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Render metadata-driven property fields
                if (definition != null && definition.properties.isNotEmpty()) {
                    definition.properties.forEach { prop ->
                        when (prop) {
                            is PropertyDefinition.Text -> {
                                var textVal by remember(node.properties[prop.name]) {
                                    mutableStateOf(node.properties[prop.name] ?: prop.defaultValue)
                                }
                                OutlinedTextField(
                                    value = textVal,
                                    onValueChange = {
                                        textVal = it
                                        val newProps = node.properties.toMutableMap()
                                        newProps[prop.name] = it
                                        onUpdateNode(node.copy(properties = newProps))
                                    },
                                    label = { Text(prop.label) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            is PropertyDefinition.BooleanProp -> {
                                var boolVal by remember(node.properties[prop.name]) {
                                    mutableStateOf(node.properties[prop.name]?.toBooleanStrictOrNull() ?: prop.defaultValue)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(prop.label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Switch(
                                        checked = boolVal,
                                        onCheckedChange = {
                                            boolVal = it
                                            val newProps = node.properties.toMutableMap()
                                            newProps[prop.name] = it.toString()
                                            onUpdateNode(node.copy(properties = newProps))
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            is PropertyDefinition.Options -> {
                                Text(prop.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    prop.options.forEach { opt ->
                                        val isCurrent = (node.properties[prop.name] ?: prop.defaultValue) == opt
                                        FilterChip(
                                            selected = isCurrent,
                                            onClick = {
                                                val newProps = node.properties.toMutableMap()
                                                newProps[prop.name] = opt
                                                onUpdateNode(node.copy(properties = newProps))
                                            },
                                            label = { Text(opt) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AeroCyan,
                                                selectedLabelColor = Color(0xFF042F2E)
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            is PropertyDefinition.Number -> {
                                val currentF: Float = node.properties[prop.name]?.toFloatOrNull() ?: prop.defaultValue
                                var numVal by remember(node.properties[prop.name]) {
                                    mutableFloatStateOf(currentF)
                                }
                                Column {
                                    Text("${prop.label}: ${numVal.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    Slider(
                                        value = numVal,
                                        onValueChange = {
                                            numVal = it
                                            val newProps = node.properties.toMutableMap()
                                            newProps[prop.name] = it.toString()
                                            onUpdateNode(node.copy(properties = newProps))
                                        },
                                        valueRange = prop.min..prop.max
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            is PropertyDefinition.ColorProp -> {
                                Text(prop.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                val colors = listOf("#F9FAFB", "#38BDF8", "#818CF8", "#34D399", "#FBBF24", "#F87171")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    colors.forEach { hex ->
                                        val c = Color(android.graphics.Color.parseColor(hex))
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(1.dp, if ((node.properties[prop.name] ?: "#F9FAFB") == hex) AeroCyan else DarkBorder, CircleShape)
                                                .clickable {
                                                    val newProps = node.properties.toMutableMap()
                                                    newProps[prop.name] = hex
                                                    onUpdateNode(node.copy(properties = newProps))
                                                }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No custom properties for this container. Use the Modifiers tab to customize size and padding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ModifierEditorView(
    node: ComponentNode,
    onUpdateModifier: (ModifierModel) -> Unit
) {
    var modifier by remember(node.modifier) { mutableStateOf(node.modifier) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Modifier Editor",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AeroCyan
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Size
                Text("Dimensions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fill Max Width", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Switch(
                        checked = modifier.fillMaxWidth,
                        onCheckedChange = {
                            modifier = modifier.copy(fillMaxWidth = it)
                            onUpdateModifier(modifier)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fill Max Height", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Switch(
                        checked = modifier.fillMaxHeight,
                        onCheckedChange = {
                            modifier = modifier.copy(fillMaxHeight = it)
                            onUpdateModifier(modifier)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Padding
                Text("Padding: ${modifier.paddingAll} dp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = modifier.paddingAll.toFloat(),
                    onValueChange = {
                        modifier = modifier.copy(paddingAll = it.toInt())
                        onUpdateModifier(modifier)
                    },
                    valueRange = 0f..48f,
                    steps = 11
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Shape
                Text("Shape & Corner Radius", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("None", "Rounded", "Circle").forEach { shapeOpt ->
                        FilterChip(
                            selected = modifier.shape == shapeOpt,
                            onClick = {
                                modifier = modifier.copy(shape = shapeOpt)
                                onUpdateModifier(modifier)
                            },
                            label = { Text(shapeOpt) }
                        )
                    }
                }

                if (modifier.shape == "Rounded") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Corner Radius: ${modifier.cornerRadius} dp", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Slider(
                        value = modifier.cornerRadius.toFloat(),
                        onValueChange = {
                            modifier = modifier.copy(cornerRadius = it.toInt())
                            onUpdateModifier(modifier)
                        },
                        valueRange = 4f..32f
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentInsertDialog(
    targetNode: ComponentNode,
    onDismiss: () -> Unit,
    onSelectComponent: (ComposeComponentDefinition) -> Unit
) {
    val colors = AeroTheme.colors
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Layout", "Text", "Input", "Lists", "Navigation", "Media")

    val filtered = remember(selectedCategory) {
        if (selectedCategory == "All") ComponentCatalog.DEFINITIONS
        else ComponentCatalog.DEFINITIONS.filter { it.category == selectedCategory }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Insert Component", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                    Text(
                        text = "Inserting inside: ${targetNode.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                            selectedLabelColor = colors.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filtered.forEach { def ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, colors.borderGlass, RoundedCornerShape(12.dp))
                            .clickable { onSelectComponent(def) },
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(def.name.take(1), fontWeight = FontWeight.Bold, color = colors.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(def.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                                Text(def.category, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = colors.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun isContainer(type: String): Boolean {
    return type in listOf("Root", "Column", "Row", "Box", "Card", "Surface", "Scaffold", "LazyColumn")
}
