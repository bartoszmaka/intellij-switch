package dev.bartoszmaka.toggle.provider

import org.junit.Test
import org.junit.Assert.assertNotNull

class StringQuoteProviderTest {
    @Test
    fun testProvider() {
        val provider = StringQuoteProvider()
        assertNotNull(provider)
    }
}
