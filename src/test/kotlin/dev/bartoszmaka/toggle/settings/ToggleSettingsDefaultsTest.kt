package dev.bartoszmaka.toggle.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleSettingsDefaultsTest {

    @Test
    fun fresh_settings_include_language_defaults_before_global_defaults() {
        val settings = ToggleSettings()

        val rules = settings.effectiveRulesFor("Python")
        val pythonIndex = rules.wordGroups.indexOfFirst { it.items == listOf("True", "False") }
        val globalIndex = rules.wordGroups.indexOfFirst { it.items == listOf("true", "false") }

        assertEquals(0, pythonIndex)
        assertTrue(globalIndex > pythonIndex)
    }
}
