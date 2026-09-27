package com.aerotech.aerocode.core.compiler

import com.aerotech.aerocode.domain.visual.ComponentNode
import com.aerotech.aerocode.domain.visual.ModifierModel

object ComposeCodeGenerator {

    /**
     * Generates a clean Compose block for a given node and its children.
     */
    fun generateNodeCode(node: ComponentNode, indentLevel: Int = 1): String {
        val indent = "    ".repeat(indentLevel)
        val childIndent = "    ".repeat(indentLevel + 1)
        val modifierCode = generateModifierCode(node.modifier)

        return when (node.type) {
            "Text" -> {
                val text = node.properties["text"] ?: "Hello AeroCode"
                val fontSize = node.properties["fontSize"]
                val fontWeight = node.properties["fontWeight"]
                val color = node.properties["color"]

                val args = mutableListOf("\"$text\"")
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")
                if (fontSize != null && fontSize != "16sp") args.add("fontSize = $fontSize")
                if (fontWeight != null && fontWeight != "Normal") args.add("fontWeight = FontWeight.$fontWeight")
                if (color != null && color.startsWith("#")) args.add("color = Color(${color.replace("#", "0xFF")})")

                "$indent${node.type}(${args.joinToString(", ")})"
            }

            "Button", "OutlinedButton", "TextButton" -> {
                val label = node.properties["label"] ?: "Button"
                val enabled = node.properties["enabled"]?.toBooleanStrictOrNull() ?: true
                val args = mutableListOf("onClick = {}")
                if (!enabled) args.add("enabled = false")
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")

                buildString {
                    appendLine("$indent${node.type}(${args.joinToString(", ")}) {")
                    if (node.children.isNotEmpty()) {
                        node.children.forEach { child ->
                            appendLine(generateNodeCode(child, indentLevel + 1))
                        }
                    } else {
                        appendLine("$childIndent${"Text(\"$label\")"}")
                    }
                    append("$indent}")
                }
            }

            "TextField", "OutlinedTextField" -> {
                val value = node.properties["value"] ?: ""
                val label = node.properties["label"] ?: "Label"
                val args = mutableListOf("value = \"$value\"", "onValueChange = {}")
                if (label.isNotEmpty()) args.add("label = { Text(\"$label\") }")
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")

                "$indent${node.type}(${args.joinToString(", ")})"
            }

            "Switch" -> {
                val checked = node.properties["checked"]?.toBooleanStrictOrNull() ?: true
                val args = mutableListOf("checked = $checked", "onCheckedChange = {}")
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")
                "$indent${node.type}(${args.joinToString(", ")})"
            }

            "Checkbox" -> {
                val checked = node.properties["checked"]?.toBooleanStrictOrNull() ?: true
                val args = mutableListOf("checked = $checked", "onCheckedChange = {}")
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")
                "$indent${node.type}(${args.joinToString(", ")})"
            }

            "Slider" -> {
                val value = node.properties["value"]?.toFloatOrNull() ?: 0.5f
                val args = mutableListOf("value = ${value}f", "onValueChange = {}")
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")
                "$indent${node.type}(${args.joinToString(", ")})"
            }

            "Spacer" -> {
                val height = node.properties["height"] ?: "16.dp"
                "$indent Spacer(modifier = Modifier.height($height))"
            }

            "Column", "Row", "Box", "Card", "Surface", "Scaffold", "LazyColumn" -> {
                val args = mutableListOf<String>()
                if (modifierCode.isNotEmpty()) args.add("modifier = $modifierCode")
                val argString = if (args.isNotEmpty()) "(\n$childIndent${args.joinToString(",\n$childIndent")}\n$indent)" else ""

                buildString {
                    appendLine("$indent${node.type}$argString {")
                    node.children.forEach { child ->
                        appendLine(generateNodeCode(child, indentLevel + 1))
                    }
                    append("$indent}")
                }
            }

            else -> {
                // Default container/element fallback
                buildString {
                    appendLine("$indent${node.type} {")
                    node.children.forEach { child ->
                        appendLine(generateNodeCode(child, indentLevel + 1))
                    }
                    append("$indent}")
                }
            }
        }
    }

    private fun generateModifierCode(modifier: ModifierModel): String {
        val parts = mutableListOf<String>()
        if (modifier.fillMaxSize) {
            parts.add("fillMaxSize()")
        } else {
            if (modifier.fillMaxWidth) parts.add("fillMaxWidth()")
            if (modifier.fillMaxHeight) parts.add("fillMaxHeight()")
        }

        if (modifier.paddingAll > 0) {
            parts.add("padding(${modifier.paddingAll}.dp)")
        } else if (modifier.paddingTop > 0 || modifier.paddingBottom > 0 || modifier.paddingStart > 0 || modifier.paddingEnd > 0) {
            val padArgs = mutableListOf<String>()
            if (modifier.paddingStart > 0) padArgs.add("start = ${modifier.paddingStart}.dp")
            if (modifier.paddingTop > 0) padArgs.add("top = ${modifier.paddingTop}.dp")
            if (modifier.paddingEnd > 0) padArgs.add("end = ${modifier.paddingEnd}.dp")
            if (modifier.paddingBottom > 0) padArgs.add("bottom = ${modifier.paddingBottom}.dp")
            parts.add("padding(${padArgs.joinToString(", ")})")
        }

        if (modifier.backgroundColor != null && modifier.backgroundColor.startsWith("#")) {
            parts.add("background(Color(${modifier.backgroundColor.replace("#", "0xFF")}))")
        }

        if (modifier.shape == "Rounded") {
            parts.add("clip(RoundedCornerShape(${modifier.cornerRadius}.dp))")
        } else if (modifier.shape == "Circle") {
            parts.add("clip(CircleShape)")
        }

        return if (parts.isEmpty()) "" else "Modifier." + parts.joinToString(".")
    }

    private val ModifierModel.fillMaxSize: Boolean
        get() = fillMaxWidth && fillMaxHeight

    /**
     * Replaces the composable function body in the original source code with the regenerated AST.
     */
    fun updateSourceWithAST(originalSource: String, rootNode: ComponentNode): String {
        val generatedChildrenCode = rootNode.children.joinToString("\n\n") { child ->
            generateNodeCode(child, indentLevel = 1)
        }

        val lines = originalSource.lines().toMutableList()
        var composableStartIndex = -1
        var composableEndIndex = -1

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.startsWith("fun App(") || line.startsWith("fun CounterApp(") ||
                line.startsWith("fun StoreScreen(") || (line.startsWith("fun ") && line.contains("()"))
            ) {
                composableStartIndex = i
                break
            }
        }

        if (composableStartIndex != -1) {
            // Find opening brace
            var openBraceIndex = composableStartIndex
            while (openBraceIndex < lines.size && !lines[openBraceIndex].contains("{")) {
                openBraceIndex++
            }
            // Find matching closing brace
            var count = 0
            for (i in openBraceIndex until lines.size) {
                for (ch in lines[i]) {
                    if (ch == '{') count++
                    else if (ch == '}') {
                        count--
                        if (count == 0) {
                            composableEndIndex = i
                            break
                        }
                    }
                }
                if (composableEndIndex != -1) break
            }

            if (composableEndIndex != -1) {
                val functionHeader = lines.subList(composableStartIndex, openBraceIndex + 1).joinToString("\n")
                val prefix = lines.subList(0, composableStartIndex).joinToString("\n")
                val suffix = lines.subList(composableEndIndex + 1, lines.size).joinToString("\n")

                val updatedFunction = buildString {
                    appendLine(functionHeader)
                    appendLine(generatedChildrenCode)
                    append("}")
                }

                return buildString {
                    if (prefix.isNotBlank()) {
                        appendLine(prefix)
                    }
                    appendLine(updatedFunction)
                    if (suffix.isNotBlank()) {
                        appendLine(suffix)
                    }
                }.trimEnd() + "\n"
            }
        }

        // Fallback: append or replace
        return originalSource
    }
}
