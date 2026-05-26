package dev.bartoszmaka.switch.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwitchSettingsDefaultsTest {

    @Test
    fun fresh_settings_include_language_defaults_before_global_defaults() {
        val settings = SwitchSettings()

        val rules = settings.effectiveRulesFor("Python")
        val pythonIndex = rules.wordGroups.indexOfFirst { it.items == listOf("True", "False") }
        val globalIndex = rules.wordGroups.indexOfFirst { it.items == listOf("true", "false") }

        assertEquals(0, pythonIndex)
        assertTrue(globalIndex > pythonIndex)
    }
}
