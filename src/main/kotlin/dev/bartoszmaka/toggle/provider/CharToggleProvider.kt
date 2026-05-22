package dev.bartoszmaka.toggle.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class CharToggleProvider : ToggleProvider {

    override fun findToggle(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): ToggleMatch? {
        val text = editor.document.text
        if (text.isEmpty()) return null

        // Try char at caret
        if (caretOffset < text.length) {
            val ch = text[caretOffset]
            val match = findMatchInGroups(ch.toString(), rules.charGroups)
            if (match != null) {
                return ToggleMatch(TextRange(caretOffset, caretOffset + 1), match)
            }
        }

        // Try char just before caret
        if (caretOffset > 0) {
            val ch = text[caretOffset - 1]
            val match = findMatchInGroups(ch.toString(), rules.charGroups)
            if (match != null) {
                return ToggleMatch(TextRange(caretOffset - 1, caretOffset), match)
            }
        }

        return null
    }

    private fun findMatchInGroups(ch: String, groups: List<ToggleGroup>): String? {
        for (group in groups) {
            val idx = group.items.indexOf(ch)
            if (idx >= 0) {
                val nextIdx = (idx + 1) % group.items.size
                return group.items[nextIdx]
            }
        }
        return null
    }
}
