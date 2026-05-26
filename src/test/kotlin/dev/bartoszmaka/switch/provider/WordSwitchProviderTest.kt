package dev.bartoszmaka.switch.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordSwitchProviderTest {

    private val lowerGroup = SwitchGroup(listOf("true", "false"))
    private val mixedGroup = SwitchGroup(listOf("True", "False"))
    private val triCycle = SwitchGroup(listOf("nil", "false", "true"))

    private fun switch(text: String, caretAt: Int, groups: List<SwitchGroup>) =
        WordSwitchProvider.findSwitchInText(text, caretAt, groups)

    @Test fun switchs_simple_lowercase() {
        val m = switch("x = true", 5, listOf(lowerGroup))!!
        assertEquals(4, m.start)
        assertEquals(8, m.endExclusive)
        assertEquals("false", m.replacement)
    }

    @Test fun switchs_caret_inside_word() {
        val m = switch("x = true", 6, listOf(lowerGroup))!!
        assertEquals("false", m.replacement)
    }

    @Test fun switchs_caret_at_word_end() {
        // caret just past the 'e' in 'true'
        val m = switch("true", 4, listOf(lowerGroup))!!
        assertEquals("false", m.replacement)
    }

    @Test fun preserves_uppercase() {
        val m = switch("X = TRUE", 5, listOf(lowerGroup))!!
        assertEquals("FALSE", m.replacement)
    }

    @Test fun preserves_titlecase() {
        val m = switch("x = True", 5, listOf(lowerGroup))!!
        assertEquals("False", m.replacement)
    }

    @Test fun mixed_case_group_is_literal_only() {
        // mixedGroup is [True, False]; "true" (all-lowercase) must NOT match
        assertNull(switch("x = true", 5, listOf(mixedGroup)))
        val m = switch("x = True", 5, listOf(mixedGroup))!!
        assertEquals("False", m.replacement)
    }

    @Test fun three_cycle_wraps() {
        val m1 = switch("nil", 0, listOf(triCycle))!!
        assertEquals("false", m1.replacement)
        val m2 = switch("false", 0, listOf(triCycle))!!
        assertEquals("true", m2.replacement)
        val m3 = switch("true", 0, listOf(triCycle))!!
        assertEquals("nil", m3.replacement)
    }

    @Test fun returns_null_when_word_not_in_any_group() {
        assertNull(switch("x = banana", 5, listOf(lowerGroup)))
    }

    @Test fun returns_null_when_caret_not_on_word() {
        assertNull(switch("   x   ", 1, listOf(lowerGroup)))
    }

    @Test fun first_match_wins() {
        val ga = SwitchGroup(listOf("on", "off"))
        val gb = SwitchGroup(listOf("on", "ready"))
        val m = switch("on", 0, listOf(ga, gb))!!
        assertEquals("off", m.replacement) // ga checked first
    }

    @Test fun underscore_identifier() {
        val grp = SwitchGroup(listOf("foo_bar", "baz_qux"))
        val m = switch("foo_bar", 2, listOf(grp))!!
        assertEquals("baz_qux", m.replacement)
    }

    @Test fun digits_prevent_all_lowercase_casing_preservation() {
        val grp = SwitchGroup(listOf("low1", "high2"))
        // "low1" with all-lowercase ASCII letters but digits is NOT all-lowercase ASCII
        // (the digit '1' is not in 'a'..'z'), so casing is literal-match only.
        // "LOW1" should NOT match (mixed case literal doesn't match "low1" or "high2").
        val m = WordSwitchProvider.findSwitchInText("LOW1", 0, listOf(grp))
        assertNull(m)
    }
}
