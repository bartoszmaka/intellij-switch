package dev.bartoszmaka.toggle.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ToggleSettingsConfigurable : Configurable {

    private var component: ToggleSettingsComponent? = null

    override fun getDisplayName(): String = "Toggle"

    override fun createComponent(): JComponent {
        val c = ToggleSettingsComponent()
        component = c
        return c.panel
    }

    override fun isModified(): Boolean = component?.isModified() == true

    override fun apply() {
        component?.apply()
    }

    override fun reset() {
        component?.reset()
    }

    override fun disposeUIResources() {
        component = null
    }
}
