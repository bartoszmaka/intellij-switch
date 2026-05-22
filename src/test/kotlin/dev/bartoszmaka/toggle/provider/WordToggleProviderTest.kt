package dev.bartoszmaka.toggle.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordToggleProviderTest {

    private val lowerGroup = ToggleGroup(listOf("true", "false"))
    private val mixedGroup = ToggleGroup(listOf("True", "False"))
    private val triCycle = ToggleGroup(listOf("nil", "false", "true"))

    private fun toggle(text: String, caretAt: Int, groups: List<ToggleGroup>) =
        WordToggleProvider.findToggleInText(text, caretAt, groups)

    @Test fun toggles_simple_lowercase() {
        val m = toggle("x = true", 5, listOf(lowerGroup))!!
        assertEquals(4, m.start)
        assertEquals(8, m.endExclusive)
        assertEquals("false", m.replacement)
    }

    @Test fun toggles_caret_inside_word() {
        val m = toggle("x = true", 6, listOf(lowerGroup))!!
        assertEquals("false", m.replacement)
    }

    @Test fun toggles_caret_at_word_end() {
        // caret just past the 'e' in 'true'
        val m = toggle("true", 4, listOf(lowerGroup))!!
        assertEquals("false", m.replacement)
    }

    @Test fun preserves_uppercase() {
        val m = toggle("X = TRUE", 5, listOf(lowerGroup))!!
        assertEquals("FALSE", m.replacement)
    }

    @Test fun preserves_titlecase() {
        val m = toggle("x = True", 5, listOf(lowerGroup))!!
        assertEquals("False", m.replacement)
    }

    @Test fun mixed_case_group_is_literal_only() {
        // mixedGroup is [True, False]; "true" (all-lowercase) must NOT match
        assertNull(toggle("x = true", 5, listOf(mixedGroup)))
        val m = toggle("x = True", 5, listOf(mixedGroup))!!
        assertEquals("False", m.replacement)
    }

    @Test fun three_cycle_wraps() {
        val m1 = toggle("nil", 0, listOf(triCycle))!!
        assertEquals("false", m1.replacement)
        val m2 = toggle("false", 0, listOf(triCycle))!!
        assertEquals("true", m2.replacement)
        val m3 = toggle("true", 0, listOf(triCycle))!!
        assertEquals("nil", m3.replacement)
    }

    @Test fun returns_null_when_word_not_in_any_group() {
        assertNull(toggle("x = banana", 5, listOf(lowerGroup)))
    }

    @Test fun returns_null_when_caret_not_on_word() {
        assertNull(toggle("   x   ", 1, listOf(lowerGroup)))
    }

    @Test fun first_match_wins() {
        val ga = ToggleGroup(listOf("on", "off"))
        val gb = ToggleGroup(listOf("on", "ready"))
        val m = toggle("on", 0, listOf(ga, gb))!!
        assertEquals("off", m.replacement) // ga checked first
    }

    @Test fun underscore_identifier() {
        val grp = ToggleGroup(listOf("foo_bar", "baz_qux"))
        val m = toggle("foo_bar", 2, listOf(grp))!!
        assertEquals("baz_qux", m.replacement)
    }

    @Test fun digits_prevent_all_lowercase_casing_preservation() {
        val grp = ToggleGroup(listOf("low1", "high2"))
        // "low1" with all-lowercase ASCII letters but digits is NOT all-lowercase ASCII
        // (the digit '1' is not in 'a'..'z'), so casing is literal-match only.
        // "LOW1" should NOT match (mixed case literal doesn't match "low1" or "high2").
        val m = WordToggleProvider.findToggleInText("LOW1", 0, listOf(grp))
        assertNull(m)
    }
}
