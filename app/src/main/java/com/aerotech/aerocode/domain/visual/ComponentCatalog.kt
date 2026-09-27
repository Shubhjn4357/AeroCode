package com.aerotech.aerocode.domain.visual

object ComponentCatalog {

    val DEFINITIONS: List<ComposeComponentDefinition> = listOf(
        // Layout
        ComposeComponentDefinition(
            id = "material3.column",
            name = "Column",
            category = "Layout",
            importPath = "androidx.compose.foundation.layout.Column",
            allowsChildren = true,
            template = """
Column(
    modifier = Modifier.fillMaxWidth()
) {
}
            """.trimIndent(),
            properties = listOf(
                PropertyDefinition.Options("horizontalAlignment", "Horizontal Alignment", listOf("Start", "CenterHorizontally", "End")),
                PropertyDefinition.Options("verticalArrangement", "Vertical Arrangement", listOf("Top", "Center", "Bottom", "SpaceBetween", "SpaceEvenly"))
            )
        ),
        ComposeComponentDefinition(
            id = "material3.row",
            name = "Row",
            category = "Layout",
            importPath = "androidx.compose.foundation.layout.Row",
            allowsChildren = true,
            template = """
Row(
    modifier = Modifier.fillMaxWidth()
) {
}
            """.trimIndent(),
            properties = listOf(
                PropertyDefinition.Options("verticalAlignment", "Vertical Alignment", listOf("Top", "CenterVertically", "Bottom")),
                PropertyDefinition.Options("horizontalArrangement", "Horizontal Arrangement", listOf("Start", "Center", "End", "SpaceBetween", "SpaceEvenly"))
            )
        ),
        ComposeComponentDefinition(
            id = "material3.box",
            name = "Box",
            category = "Layout",
            importPath = "androidx.compose.foundation.layout.Box",
            allowsChildren = true,
            template = """
Box(
    modifier = Modifier.fillMaxSize()
) {
}
            """.trimIndent(),
            properties = listOf(
                PropertyDefinition.Options("contentAlignment", "Content Alignment", listOf("TopStart", "Center", "BottomEnd"))
            )
        ),
        ComposeComponentDefinition(
            id = "material3.card",
            name = "Card",
            category = "Layout",
            importPath = "androidx.compose.material3.Card",
            allowsChildren = true,
            template = """
Card(
    modifier = Modifier.fillMaxWidth()
) {
}
            """.trimIndent(),
            properties = listOf(
                PropertyDefinition.Number("elevation", "Elevation (dp)", 2f, 0f, 16f)
            )
        ),
        ComposeComponentDefinition(
            id = "material3.surface",
            name = "Surface",
            category = "Layout",
            importPath = "androidx.compose.material3.Surface",
            allowsChildren = true,
            template = """
Surface(
    modifier = Modifier.fillMaxSize()
) {
}
            """.trimIndent(),
            properties = emptyList()
        ),

        // Text
        ComposeComponentDefinition(
            id = "material3.text",
            name = "Text",
            category = "Text",
            importPath = "androidx.compose.material3.Text",
            template = """Text("Hello AeroCode")""",
            properties = listOf(
                PropertyDefinition.Text("text", "Text Content", "Hello AeroCode"),
                PropertyDefinition.Options("fontSize", "Font Size", listOf("12sp", "14sp", "16sp", "18sp", "20sp", "24sp", "32sp")),
                PropertyDefinition.Options("fontWeight", "Weight", listOf("Normal", "Medium", "SemiBold", "Bold")),
                PropertyDefinition.Options("textAlign", "Alignment", listOf("Start", "Center", "End")),
                PropertyDefinition.ColorProp("color", "Text Color", "#F9FAFB")
            )
        ),

        // Input
        ComposeComponentDefinition(
            id = "material3.button",
            name = "Button",
            category = "Input",
            importPath = "androidx.compose.material3.Button",
            allowsChildren = true,
            template = """
Button(onClick = {}) {
    Text("Button")
}
            """.trimIndent(),
            properties = listOf(
                PropertyDefinition.Text("label", "Label", "Button"),
                PropertyDefinition.BooleanProp("enabled", "Enabled", true)
            )
        ),
        ComposeComponentDefinition(
            id = "material3.outlined_button",
            name = "OutlinedButton",
            category = "Input",
            importPath = "androidx.compose.material3.OutlinedButton",
            allowsChildren = true,
            template = """
OutlinedButton(onClick = {}) {
    Text("Outlined")
}
            """.trimIndent(),
            properties = listOf(
                PropertyDefinition.Text("label", "Label", "Outlined"),
                PropertyDefinition.BooleanProp("enabled", "Enabled", true)
            )
        ),
        ComposeComponentDefinition(
            id = "material3.textfield",
            name = "TextField",
            category = "Input",
            importPath = "androidx.compose.material3.TextField",
            template = """TextField(value = "Input text", onValueChange = {}, label = { Text("Label") })""",
            properties = listOf(
                PropertyDefinition.Text("value", "Value", "Input text"),
                PropertyDefinition.Text("label", "Label", "Label"),
                PropertyDefinition.BooleanProp("enabled", "Enabled", true),
                PropertyDefinition.BooleanProp("singleLine", "Single Line", true)
            )
        ),
        ComposeComponentDefinition(
            id = "material3.switch",
            name = "Switch",
            category = "Input",
            importPath = "androidx.compose.material3.Switch",
            template = """Switch(checked = true, onCheckedChange = {})""",
            properties = listOf(
                PropertyDefinition.BooleanProp("checked", "Checked", true),
                PropertyDefinition.BooleanProp("enabled", "Enabled", true)
            )
        ),
        ComposeComponentDefinition(
            id = "material3.checkbox",
            name = "Checkbox",
            category = "Input",
            importPath = "androidx.compose.material3.Checkbox",
            template = """Checkbox(checked = true, onCheckedChange = {})""",
            properties = listOf(
                PropertyDefinition.BooleanProp("checked", "Checked", true),
                PropertyDefinition.BooleanProp("enabled", "Enabled", true)
            )
        ),
        ComposeComponentDefinition(
            id = "material3.slider",
            name = "Slider",
            category = "Input",
            importPath = "androidx.compose.material3.Slider",
            template = """Slider(value = 0.5f, onValueChange = {})""",
            properties = listOf(
                PropertyDefinition.Number("value", "Slider Value", 0.5f, 0f, 1f),
                PropertyDefinition.BooleanProp("enabled", "Enabled", true)
            )
        ),

        // Lists
        ComposeComponentDefinition(
            id = "material3.lazy_column",
            name = "LazyColumn",
            category = "Lists",
            importPath = "androidx.compose.foundation.lazy.LazyColumn",
            allowsChildren = true,
            template = """
LazyColumn(
    modifier = Modifier.fillMaxSize()
) {
    items(5) { index ->
        Text("Item #${'$'}index")
    }
}
            """.trimIndent(),
            properties = emptyList()
        ),

        // Navigation
        ComposeComponentDefinition(
            id = "material3.scaffold",
            name = "Scaffold",
            category = "Navigation",
            importPath = "androidx.compose.material3.Scaffold",
            allowsChildren = true,
            template = """
Scaffold(
    topBar = {
        TopAppBar(title = { Text("AeroCode") })
    }
) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
    }
}
            """.trimIndent(),
            properties = emptyList()
        ),

        // Media
        ComposeComponentDefinition(
            id = "material3.icon",
            name = "Icon",
            category = "Media",
            importPath = "androidx.compose.material3.Icon",
            template = """Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite")""",
            properties = listOf(
                PropertyDefinition.Text("contentDescription", "Description", "Favorite")
            )
        )
    )

    fun findDefinition(type: String): ComposeComponentDefinition? {
        return DEFINITIONS.find { it.name.equals(type, ignoreCase = true) }
    }
}
