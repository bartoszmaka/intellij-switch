package dev.bartoszmaka.toggle.provider

import org.junit.Test
import org.junit.Assert.assertNotNull

class CharToggleProviderTest {
    @Test
    fun testProvider() {
        val provider = CharToggleProvider()
        assertNotNull(provider)
    }
}
