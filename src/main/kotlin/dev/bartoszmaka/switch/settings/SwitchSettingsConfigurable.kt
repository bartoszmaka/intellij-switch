package dev.bartoszmaka.switch.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class SwitchSettingsConfigurable : Configurable {

    private var component: SwitchSettingsComponent? = null

    override fun getDisplayName(): String = "Switch"

    override fun createComponent(): JComponent {
        val c = SwitchSettingsComponent()
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
