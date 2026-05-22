package dev.bartoszmaka.toggle.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.bartoszmaka.toggle.util.Casing

class WordToggleProvider : ToggleProvider {

    override fun findToggle(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): ToggleMatch? {
        val match = findToggleInText(editor.document.text, caretOffset, rules.wordGroups)
            ?: return null
        return ToggleMatch(
            range = TextRange(match.start, match.endExclusive),
            replacement = match.replacement,
        )
    }

    data class RawMatch(val start: Int, val endExclusive: Int, val replacement: String)

    companion object {
        private fun isIdentStart(c: Char) = c == '_' || c in 'A'..'Z' || c in 'a'..'z'
        private fun isIdentPart(c: Char) = isIdentStart(c) || c in '0'..'9'

        private fun wordBoundsAt(text: String, offset: Int): Pair<Int, Int>? {
            val n = text.length
            // First decide which character (if any) the caret is "on".
            // Treat both `text[offset]` and `text[offset - 1]` as candidates.
            val onChar = offset in 0 until n && isIdentPart(text[offset])
            val behindChar = offset > 0 && offset <= n && isIdentPart(text[offset - 1])
            if (!onChar && !behindChar) return null

            // Expand from whichever side hits a word char first; prefer the one under the caret.
            val anchor = when {
                onChar -> offset
                else -> offset - 1
            }
            var start = anchor
            while (start > 0 && isIdentPart(text[start - 1])) start--
            var end = anchor
            while (end < n && isIdentPart(text[end])) end++
            // Validate identifier-start.
            if (!isIdentStart(text[start])) return null
            return start to end
        }

        private fun isAllLowercaseAscii(items: List<String>): Boolean =
            items.all { s -> s.isNotEmpty() && s.all { it in 'a'..'z' } }

        fun findToggleInText(
            text: String,
            caretOffset: Int,
            groups: List<ToggleGroup>,
        ): RawMatch? {
            val (start, end) = wordBoundsAt(text, caretOffset) ?: return null
            val word = text.substring(start, end)

            for (group in groups) {
                val lowercaseGroup = isAllLowercaseAscii(group.items)
                val matchIdx = if (lowercaseGroup) {
                    val needle = word.lowercase()
                    group.items.indexOfFirst { it == needle }
                } else {
                    group.items.indexOf(word)
                }
                if (matchIdx < 0) continue
                val next = group.items[(matchIdx + 1) % group.items.size]
                val replacement = if (lowercaseGroup) Casing.apply(next, Casing.detect(word)) else next
                return RawMatch(start, end, replacement)
            }
            return null
        }
    }
}
