package dev.bartoszmaka.toggle.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

data class ToggleGroup(val items: List<String>)

data class EffectiveRules(
    val wordGroups: List<ToggleGroup> = emptyList(),
    val charGroups: List<ToggleGroup> = emptyList(),
)

data class ToggleMatch(
    val range: TextRange,
    val replacement: String,
)

interface ToggleProvider {
    fun findToggle(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): ToggleMatch?
}
