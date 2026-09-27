package com.aerotech.aerocode.domain.visual

import com.aerotech.aerocode.domain.editor.SourceRange

sealed interface PropertyDefinition {
    val name: String
    val label: String

    data class Text(
        override val name: String,
        override val label: String,
        val defaultValue: String = ""
    ) : PropertyDefinition

    data class Number(
        override val name: String,
        override val label: String,
        val defaultValue: Float = 0f,
        val min: Float = 0f,
        val max: Float = 100f
    ) : PropertyDefinition

    data class BooleanProp(
        override val name: String,
        override val label: String,
        val defaultValue: Boolean = true
    ) : PropertyDefinition

    data class Options(
        override val name: String,
        override val label: String,
        val options: List<String>,
        val defaultValue: String = options.firstOrNull() ?: ""
    ) : PropertyDefinition

    data class ColorProp(
        override val name: String,
        override val label: String,
        val defaultValue: String = "#0284C7"
    ) : PropertyDefinition
}

data class ModifierModel(
    val fillMaxWidth: Boolean = false,
    val fillMaxHeight: Boolean = false,
    val paddingAll: Int = 0,
    val paddingTop: Int = 0,
    val paddingBottom: Int = 0,
    val paddingStart: Int = 0,
    val paddingEnd: Int = 0,
    val height: Int = 0,
    val width: Int = 0,
    val size: Int = 0,
    val weight: Float = 0f,
    val backgroundColor: String? = null,
    val shape: String = "None", // None, Rounded, Circle
    val cornerRadius: Int = 12
)

data class ComponentNode(
    val id: String,
    val type: String, // Text, Button, Column, Row, Card, etc.
    var label: String = type,
    val properties: MutableMap<String, String> = mutableMapOf(),
    var modifier: ModifierModel = ModifierModel(),
    var sourceRange: SourceRange = SourceRange(1, 0, 1, 0),
    val children: MutableList<ComponentNode> = mutableListOf(),
    var parentId: String? = null
)

data class ComposeComponentDefinition(
    val id: String,
    val name: String,
    val category: String, // Layout, Text, Input, Lists, Navigation, Media
    val importPath: String,
    val template: String,
    val properties: List<PropertyDefinition>,
    val allowsChildren: Boolean = false
)
