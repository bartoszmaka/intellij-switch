package dev.bartoszmaka.toggle.provider

import org.junit.Test
import org.junit.Assert.assertNotNull

class WordToggleProviderTest {
    @Test
    fun testProvider() {
        val provider = WordToggleProvider()
        assertNotNull(provider)
    }
}
