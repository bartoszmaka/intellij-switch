package dev.bartoszmaka.switch.settings

import dev.bartoszmaka.switch.provider.SwitchGroup

object Defaults {

    val globalWordGroups: List<SwitchGroup> = listOf(
        SwitchGroup(listOf("true", "false")),
        SwitchGroup(listOf("yes", "no")),
        SwitchGroup(listOf("on", "off")),
        SwitchGroup(listOf("define", "undef")),
        SwitchGroup(listOf("in", "out")),
        SwitchGroup(listOf("up", "down")),
        SwitchGroup(listOf("left", "right")),
        SwitchGroup(listOf("north", "south")),
        SwitchGroup(listOf("east", "west")),
        SwitchGroup(listOf("and", "or")),
        SwitchGroup(listOf("min", "max")),
        SwitchGroup(listOf("first", "last")),
    )

    val globalCharGroups: List<SwitchGroup> = listOf(
        SwitchGroup(listOf("+", "-")),
        SwitchGroup(listOf(">", "<")),
        SwitchGroup(listOf("&", "|")),
        SwitchGroup(listOf("*", "/")),
    )

    /** Per-language seeded word groups. Keys are `Language.id` values. */
    val languageWordGroups: Map<String, List<SwitchGroup>> = mapOf(
        "ruby" to listOf(
            SwitchGroup(listOf("nil", "false", "true")),
            SwitchGroup(listOf("unless", "if")),
        ),
        "JavaScript" to listOf(
            SwitchGroup(listOf("let", "const")),
            SwitchGroup(listOf("var", "let")),
            SwitchGroup(listOf("null", "undefined")),
        ),
        "TypeScript" to listOf(
            SwitchGroup(listOf("let", "const")),
            SwitchGroup(listOf("var", "let")),
            SwitchGroup(listOf("null", "undefined")),
        ),
        "Python" to listOf(
            SwitchGroup(listOf("True", "False")),
            SwitchGroup(listOf("None", "True", "False")),
            SwitchGroup(listOf("and", "or")),
        ),
    )

    val languageCharGroups: Map<String, List<SwitchGroup>> = emptyMap()
}
