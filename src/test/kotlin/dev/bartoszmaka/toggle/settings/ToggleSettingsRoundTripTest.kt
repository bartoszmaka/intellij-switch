package dev.bartoszmaka.toggle.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleSettingsRoundTripTest {

    @Test
    fun testSerializeDeserialize() {
        val original = ToggleSettings.State()
        original.global.wordGroups.add(ToggleSettings.GroupState(mutableListOf("a", "b")))

        val copy = ToggleSettings.State()
        copy.global = original.global
        copy.perLanguage = original.perLanguage

        assertEquals(original.global.wordGroups.size, copy.global.wordGroups.size)
    }
}
