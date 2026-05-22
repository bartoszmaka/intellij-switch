package dev.bartoszmaka.toggle.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharToggleProviderTest {

    private val plusMinus = ToggleGroup(listOf("+", "-"))
    private val ltGt = ToggleGroup(listOf(">", "<"))

    private fun toggle(text: String, caret: Int, groups: List<ToggleGroup>) =
        CharToggleProvider.findToggleInText(text, caret, groups)

    @Test fun toggles_char_at_caret() {
        val m = toggle("a + b", 2, listOf(plusMinus))!!
        assertEquals(2, m.start)
        assertEquals(3, m.endExclusive)
        assertEquals("-", m.replacement)
    }

    @Test fun toggles_char_before_caret() {
        // caret immediately past '+'
        val m = toggle("a+ b", 2, listOf(plusMinus))!!
        assertEquals(1, m.start)
        assertEquals(2, m.endExclusive)
        assertEquals("-", m.replacement)
    }

    @Test fun prefers_char_at_caret_over_before() {
        // caret on '<', char before is '>'; both match a group → prefer at-caret
        val m = toggle("><", 1, listOf(ltGt))!!
        assertEquals(1, m.start)
        assertEquals(2, m.endExclusive)
        assertEquals(">", m.replacement) // < cycles to >
    }

    @Test fun returns_null_when_no_match() {
        assertNull(toggle("abc", 1, listOf(plusMinus)))
    }

    @Test fun caret_at_eof_uses_char_before() {
        val m = toggle("+", 1, listOf(plusMinus))!!
        assertEquals(0, m.start)
        assertEquals(1, m.endExclusive)
        assertEquals("-", m.replacement)
    }

    @Test fun caret_at_start() {
        val m = toggle("+", 0, listOf(plusMinus))!!
        assertEquals(0, m.start)
        assertEquals(1, m.endExclusive)
        assertEquals("-", m.replacement)
    }

    @Test fun two_char_cycle_wraps() {
        val m = toggle("-", 0, listOf(plusMinus))!!
        assertEquals("+", m.replacement)
    }
}
