package dev.bartoszmaka.toggle.action

import dev.bartoszmaka.toggle.ToggleAction
import org.junit.Test
import org.junit.Assert.assertNotNull

class ToggleActionTest {
    @Test
    fun testAction() {
        val action = ToggleAction()
        assertNotNull(action)
    }
}
