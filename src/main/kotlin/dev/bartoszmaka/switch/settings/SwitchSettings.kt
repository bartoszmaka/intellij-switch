package dev.bartoszmaka.switch.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.bartoszmaka.switch.provider.EffectiveRules
import dev.bartoszmaka.switch.provider.SwitchGroup

@Service
@State(
    name = "Switch",
    storages = [Storage("switch.xml")]
)
class SwitchSettings : PersistentStateComponent<SwitchSettings.State> {

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    fun getEffectiveRules(langId: String): EffectiveRules = effectiveRulesFor(langId)

    fun effectiveRulesFor(langId: String): EffectiveRules {
        val lang = state.perLanguage[langId]
        val langWord = lang?.wordGroups.orEmpty()
            .mapNotNull { it.toSwitchGroupOrNull(word = true) }
        val langChar = lang?.charGroups.orEmpty()
            .mapNotNull { it.toSwitchGroupOrNull(word = false) }
        val inherit = lang?.inheritsGlobal ?: true
        val globalWord = if (inherit) state.global.wordGroups
            .mapNotNull { it.toSwitchGroupOrNull(word = true) } else emptyList()
        val globalChar = if (inherit) state.global.charGroups
            .mapNotNull { it.toSwitchGroupOrNull(word = false) } else emptyList()
        return EffectiveRules(
            wordGroups = langWord + globalWord,
            charGroups = langChar + globalChar,
        )
    }

    private fun GroupState.toSwitchGroupOrNull(word: Boolean): SwitchGroup? {
        val items = items.toList()
        val errs = if (word) SwitchGroupValidation.validateWordGroup(items)
                   else SwitchGroupValidation.validateCharGroup(items)
        if (errs.isNotEmpty()) {
            thisLogger().warn("Skipping invalid switch group: $items — ${errs.joinToString("; ")}")
            return null
        }
        return SwitchGroup(items)
    }

    data class State(
        var global: LanguageRulesState = defaultGlobalState(),
        var perLanguage: MutableMap<String, LanguageRulesState> = defaultPerLanguageState(),
    )

    data class LanguageRulesState(
        var wordGroups: MutableList<GroupState> = mutableListOf(),
        var charGroups: MutableList<GroupState> = mutableListOf(),
        var inheritsGlobal: Boolean = true,
    )

    data class GroupState(
        var items: MutableList<String> = mutableListOf(),
    )

    companion object {
        fun getInstance(): SwitchSettings =
            ApplicationManager.getApplication().getService(SwitchSettings::class.java)

        fun defaultGlobalState(): LanguageRulesState = LanguageRulesState(
            wordGroups = Defaults.globalWordGroups
                .map { GroupState(it.items.toMutableList()) }
                .toMutableList(),
            charGroups = Defaults.globalCharGroups
                .map { GroupState(it.items.toMutableList()) }
                .toMutableList(),
            inheritsGlobal = false,
        )

        fun defaultPerLanguageState(): MutableMap<String, LanguageRulesState> {
            val state = mutableMapOf<String, LanguageRulesState>()
            val languages = Defaults.languageWordGroups.keys + Defaults.languageCharGroups.keys
            for (language in languages) {
                state[language] = LanguageRulesState(
                    wordGroups = Defaults.languageWordGroups[language].orEmpty()
                        .map { GroupState(it.items.toMutableList()) }
                        .toMutableList(),
                    charGroups = Defaults.languageCharGroups[language].orEmpty()
                        .map { GroupState(it.items.toMutableList()) }
                        .toMutableList(),
                    inheritsGlobal = true,
                )
            }
            return state
        }
    }
}

typealias LanguageRulesState = SwitchSettings.LanguageRulesState
typealias GroupState = SwitchSettings.GroupState
