package dev.bartoszmaka.toggle.settings

import com.intellij.util.xmlb.XmlSerializer
import dev.bartoszmaka.toggle.provider.EffectiveRules
import dev.bartoszmaka.toggle.provider.ToggleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleSettingsRoundTripTest {

    @Test fun round_trip_preserves_state() {
        val original = ToggleSettings.State().apply {
            global = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("true", "false"))),
                charGroups = mutableListOf(GroupState(mutableListOf("+", "-"))),
                inheritsGlobal = false,  // ignored on global
            )
            perLanguage["Python"] = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("True", "False"))),
                charGroups = mutableListOf(),
                inheritsGlobal = true,
            )
        }

        val element = XmlSerializer.serialize(original)
        val deserialized = XmlSerializer.deserialize(element, ToggleSettings.State::class.java)

        assertEquals(original.global.wordGroups.size, deserialized.global.wordGroups.size)
        assertEquals(
            original.global.wordGroups[0].items,
            deserialized.global.wordGroups[0].items,
        )
        assertEquals(
            original.perLanguage["Python"]!!.wordGroups[0].items,
            deserialized.perLanguage["Python"]!!.wordGroups[0].items,
        )
    }

    @Test fun effective_rules_language_first_then_global() {
        val settings = ToggleSettings()
        settings.loadState(ToggleSettings.State().apply {
            global = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("true", "false"))),
                charGroups = mutableListOf(),
            )
            perLanguage["Python"] = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("True", "False"))),
                charGroups = mutableListOf(),
                inheritsGlobal = true,
            )
        })
        val rules: EffectiveRules = settings.effectiveRulesFor("Python")
        assertEquals(2, rules.wordGroups.size)
        assertEquals(listOf("True", "False"), rules.wordGroups[0].items)
        assertEquals(listOf("true", "false"), rules.wordGroups[1].items)
    }

    @Test fun effective_rules_no_inherit() {
        val settings = ToggleSettings()
        settings.loadState(ToggleSettings.State().apply {
            global = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("true", "false"))),
                charGroups = mutableListOf(),
            )
            perLanguage["Python"] = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("True", "False"))),
                charGroups = mutableListOf(),
                inheritsGlobal = false,
            )
        })
        val rules = settings.effectiveRulesFor("Python")
        assertEquals(1, rules.wordGroups.size)
        assertEquals(listOf("True", "False"), rules.wordGroups[0].items)
    }

    @Test fun effective_rules_unknown_language_returns_global() {
        val settings = ToggleSettings()
        settings.loadState(ToggleSettings.State().apply {
            global = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("true", "false"))),
                charGroups = mutableListOf(),
            )
        })
        val rules = settings.effectiveRulesFor("Whatever")
        assertEquals(1, rules.wordGroups.size)
    }

    @Test fun invalid_groups_filtered_on_load() {
        val settings = ToggleSettings()
        settings.loadState(ToggleSettings.State().apply {
            global = LanguageRulesState(
                wordGroups = mutableListOf(
                    GroupState(mutableListOf("true", "false")),
                    GroupState(mutableListOf("solo")),          // < 2 items, filtered
                    GroupState(mutableListOf("a", "is not")),   // non-identifier, filtered
                ),
                charGroups = mutableListOf(),
            )
        })
        val rules = settings.effectiveRulesFor("anything")
        assertEquals(1, rules.wordGroups.size)
        assertTrue(rules.wordGroups[0].items.contains("true"))
    }
}
