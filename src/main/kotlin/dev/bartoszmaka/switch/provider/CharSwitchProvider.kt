package dev.bartoszmaka.switch.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class CharSwitchProvider : SwitchProvider {

    override fun findSwitch(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
        reverse: Boolean,
    ): SwitchMatch? {
        val match = findSwitchInText(editor.document.text, caretOffset, rules.charGroups, reverse)
            ?: return null
        return SwitchMatch(
            range = TextRange(match.start, match.endExclusive),
            replacement = match.replacement,
        )
    }

    data class RawMatch(val start: Int, val endExclusive: Int, val replacement: String)

    companion object {
        fun findSwitchInText(
            text: String,
            caretOffset: Int,
            groups: List<SwitchGroup>,
            reverse: Boolean = false,
        ): RawMatch? {
            // Try the char at caret first.
            tryAt(text, caretOffset, groups, reverse)?.let { return it }
            // Then the char immediately before caret.
            tryAt(text, caretOffset - 1, groups, reverse)?.let { return it }
            return null
        }

        private fun tryAt(text: String, idx: Int, groups: List<SwitchGroup>, reverse: Boolean): RawMatch? {
            if (idx < 0 || idx >= text.length) return null
            val ch = text[idx].toString()
            for (group in groups) {
                val matchIdx = group.items.indexOf(ch)
                if (matchIdx < 0) continue
                val size = group.items.size
                val dir = if (reverse) -1 else 1
                val nextIdx = ((matchIdx + dir) % size + size) % size
                val next = group.items[nextIdx]
                return RawMatch(idx, idx + 1, next)
            }
            return null
        }
    }
}
