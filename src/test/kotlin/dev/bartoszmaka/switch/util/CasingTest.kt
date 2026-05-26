package dev.bartoszmaka.switch.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CasingTest {
    @Test fun detect_lower() = assertEquals(Casing.LOWER, Casing.detect("true"))
    @Test fun detect_upper() = assertEquals(Casing.UPPER, Casing.detect("TRUE"))
    @Test fun detect_title() = assertEquals(Casing.TITLE, Casing.detect("True"))
    @Test fun detect_other_mixed() = assertEquals(Casing.OTHER, Casing.detect("tRue"))
    @Test fun detect_other_single_lower() = assertEquals(Casing.LOWER, Casing.detect("a"))
    @Test fun detect_other_single_upper() = assertEquals(Casing.UPPER, Casing.detect("A"))
    @Test fun detect_other_empty() = assertEquals(Casing.OTHER, Casing.detect(""))
    @Test fun detect_other_digits() = assertEquals(Casing.OTHER, Casing.detect("abc1"))

    @Test fun apply_lower() = assertEquals("false", Casing.apply("false", Casing.LOWER))
    @Test fun apply_upper() = assertEquals("FALSE", Casing.apply("false", Casing.UPPER))
    @Test fun apply_title() = assertEquals("False", Casing.apply("false", Casing.TITLE))
    @Test fun apply_other() = assertEquals("false", Casing.apply("false", Casing.OTHER))
    @Test fun apply_title_single_letter() = assertEquals("F", Casing.apply("f", Casing.TITLE))
}
