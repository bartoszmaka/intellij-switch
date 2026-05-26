package dev.bartoszmaka.switch.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

data class SwitchGroup(val items: List<String>)

data class EffectiveRules(
    val wordGroups: List<SwitchGroup> = emptyList(),
    val charGroups: List<SwitchGroup> = emptyList(),
)

data class SwitchMatch(
    val range: TextRange,
    val replacement: String,
)

interface SwitchProvider {
    fun findSwitch(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): SwitchMatch?
}
