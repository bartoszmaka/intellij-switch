package dev.bartoszmaka.toggle.settings

import dev.bartoszmaka.toggle.provider.ToggleGroup

object Defaults {

    val globalWordGroups: List<ToggleGroup> = listOf(
        ToggleGroup(listOf("true", "false")),
        ToggleGroup(listOf("yes", "no")),
        ToggleGroup(listOf("on", "off")),
        ToggleGroup(listOf("define", "undef")),
        ToggleGroup(listOf("in", "out")),
        ToggleGroup(listOf("up", "down")),
        ToggleGroup(listOf("left", "right")),
        ToggleGroup(listOf("north", "south")),
        ToggleGroup(listOf("east", "west")),
        ToggleGroup(listOf("and", "or")),
        ToggleGroup(listOf("min", "max")),
        ToggleGroup(listOf("first", "last")),
    )

    val globalCharGroups: List<ToggleGroup> = listOf(
        ToggleGroup(listOf("+", "-")),
        ToggleGroup(listOf(">", "<")),
        ToggleGroup(listOf("&", "|")),
        ToggleGroup(listOf("*", "/")),
    )

    /** Per-language seeded word groups. Keys are `Language.id` values. */
    val languageWordGroups: Map<String, List<ToggleGroup>> = mapOf(
        "ruby" to listOf(
            ToggleGroup(listOf("nil", "false", "true")),
            ToggleGroup(listOf("unless", "if")),
        ),
        "JavaScript" to listOf(
            ToggleGroup(listOf("let", "const")),
            ToggleGroup(listOf("var", "let")),
            ToggleGroup(listOf("null", "undefined")),
        ),
        "TypeScript" to listOf(
            ToggleGroup(listOf("let", "const")),
            ToggleGroup(listOf("var", "let")),
            ToggleGroup(listOf("null", "undefined")),
        ),
        "Python" to listOf(
            ToggleGroup(listOf("True", "False")),
            ToggleGroup(listOf("None", "True", "False")),
            ToggleGroup(listOf("and", "or")),
        ),
    )

    val languageCharGroups: Map<String, List<ToggleGroup>> = emptyMap()
}
