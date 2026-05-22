package dev.bartoszmaka.toggle.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.bartoszmaka.toggle.provider.EffectiveRules
import dev.bartoszmaka.toggle.provider.ToggleGroup

@Service
@State(
    name = "Toggle",
    storages = [Storage("toggle.xml")]
)
class ToggleSettings : PersistentStateComponent<ToggleSettings.State> {

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    fun getEffectiveRules(langId: String): EffectiveRules {
        val globalWords = state.global.wordGroups.map { ToggleGroup(it.items) }
        val globalChars = state.global.charGroups.map { ToggleGroup(it.items) }
        val langRules = state.perLanguage[langId]

        val words = if (langRules?.inheritsGlobal != false) {
            (langRules?.wordGroups?.map { ToggleGroup(it.items) } ?: emptyList()) + globalWords
        } else {
            langRules?.wordGroups?.map { ToggleGroup(it.items) } ?: emptyList()
        }

        val chars = if (langRules?.inheritsGlobal != false) {
            (langRules?.charGroups?.map { ToggleGroup(it.items) } ?: emptyList()) + globalChars
        } else {
            langRules?.charGroups?.map { ToggleGroup(it.items) } ?: emptyList()
        }

        return EffectiveRules(words, chars)
    }

    data class State(
        var global: LanguageRulesState = LanguageRulesState(),
        var perLanguage: MutableMap<String, LanguageRulesState> = mutableMapOf(),
    )

    data class LanguageRulesState(
        var wordGroups: MutableList<GroupState> = mutableListOf(),
        var charGroups: MutableList<GroupState> = mutableListOf(),
        var inheritsGlobal: Boolean = true,
    )

    data class GroupState(
        var items: MutableList<String> = mutableListOf(),
    )
}
