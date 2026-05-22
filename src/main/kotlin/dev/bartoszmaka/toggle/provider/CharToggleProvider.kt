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
        val match = findToggleInText(editor.document.text, caretOffset, rules.charGroups)
            ?: return null
        return ToggleMatch(
            range = TextRange(match.start, match.endExclusive),
            replacement = match.replacement,
        )
    }

    data class RawMatch(val start: Int, val endExclusive: Int, val replacement: String)

    companion object {
        fun findToggleInText(
            text: String,
            caretOffset: Int,
            groups: List<ToggleGroup>,
        ): RawMatch? {
            // Try the char at caret first.
            tryAt(text, caretOffset, groups)?.let { return it }
            // Then the char immediately before caret.
            tryAt(text, caretOffset - 1, groups)?.let { return it }
            return null
        }

        private fun tryAt(text: String, idx: Int, groups: List<ToggleGroup>): RawMatch? {
            if (idx < 0 || idx >= text.length) return null
            val ch = text[idx].toString()
            for (group in groups) {
                val matchIdx = group.items.indexOf(ch)
                if (matchIdx < 0) continue
                val next = group.items[(matchIdx + 1) % group.items.size]
                return RawMatch(idx, idx + 1, next)
            }
            return null
        }
    }
}
