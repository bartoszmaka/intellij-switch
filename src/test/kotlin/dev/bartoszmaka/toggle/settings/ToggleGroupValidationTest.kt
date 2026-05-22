package dev.bartoszmaka.toggle.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleGroupValidationTest {

    @Test fun word_group_valid() {
        val errs = ToggleGroupValidation.validateWordGroup(listOf("true", "false"))
        assertTrue(errs.toString(), errs.isEmpty())
    }

    @Test fun word_group_rejects_whitespace() {
        val errs = ToggleGroupValidation.validateWordGroup(listOf("is", "is not"))
        assertEquals(1, errs.size)
        assertTrue(errs[0].contains("identifier"))
    }

    @Test fun word_group_rejects_empty_item() {
        val errs = ToggleGroupValidation.validateWordGroup(listOf("true", ""))
        assertTrue(errs.any { it.contains("identifier") })
    }

    @Test fun word_group_rejects_punctuation() {
        val errs = ToggleGroupValidation.validateWordGroup(listOf("a", "a-b"))
        assertTrue(errs.any { it.contains("identifier") })
    }

    @Test fun word_group_rejects_lt_two_items() {
        val errs = ToggleGroupValidation.validateWordGroup(listOf("solo"))
        assertTrue(errs.any { it.contains("at least 2") })
    }

    @Test fun word_group_rejects_duplicates() {
        val errs = ToggleGroupValidation.validateWordGroup(listOf("on", "off", "on"))
        assertTrue(errs.any { it.contains("duplicate") })
    }

    @Test fun char_group_valid() {
        val errs = ToggleGroupValidation.validateCharGroup(listOf("+", "-"))
        assertTrue(errs.toString(), errs.isEmpty())
    }

    @Test fun char_group_valid_unicode_supplementary() {
        // U+1F600 (😀) is one code point but two UTF-16 chars
        val errs = ToggleGroupValidation.validateCharGroup(listOf("😀", "x"))
        assertTrue(errs.toString(), errs.isEmpty())
    }

    @Test fun char_group_rejects_two_chars() {
        val errs = ToggleGroupValidation.validateCharGroup(listOf("==", "==="))
        assertTrue(errs.any { it.contains("single") })
    }

    @Test fun char_group_rejects_empty_item() {
        val errs = ToggleGroupValidation.validateCharGroup(listOf("+", ""))
        assertTrue(errs.any { it.contains("single") })
    }

    @Test fun char_group_rejects_whitespace_item() {
        val errs = ToggleGroupValidation.validateCharGroup(listOf("+", " "))
        assertTrue(errs.any { it.contains("whitespace") || it.contains("single") })
    }

    @Test fun char_group_rejects_lt_two_items() {
        val errs = ToggleGroupValidation.validateCharGroup(listOf("+"))
        assertTrue(errs.any { it.contains("at least 2") })
    }
}
