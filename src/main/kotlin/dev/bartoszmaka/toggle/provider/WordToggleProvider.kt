package dev.bartoszmaka.toggle.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.bartoszmaka.toggle.util.Casing

class WordToggleProvider : ToggleProvider {
    private val WORD_RX = Regex("[A-Za-z_][A-Za-z0-9_]*")

    override fun findToggle(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): ToggleMatch? {
        val text = editor.document.text
        if (caretOffset < 0 || caretOffset > text.length) return null

        // Find word boundaries around caret
        var start = caretOffset
        while (start > 0 && text[start - 1].let { it.isLetterOrDigit() || it == '_' }) {
            start--
        }
        var end = caretOffset
        while (end < text.length && text[end].let { it.isLetterOrDigit() || it == '_' }) {
            end++
        }

        if (start == end || start >= text.length) return null

        val word = text.substring(start, end)
        if (!WORD_RX.matches(word)) return null

        // Try to match in word groups
        val allGroups = rules.wordGroups
        for (group in allGroups) {
            val idx = group.items.indexOfAny(word)
            if (idx >= 0) {
                val replacement = findReplacement(word, group, idx)
                return ToggleMatch(TextRange(start, end), replacement)
            }
        }

        return null
    }

    private fun findReplacement(original: String, group: ToggleGroup, currentIdx: Int): String {
        val nextIdx = (currentIdx + 1) % group.items.size
        val nextItem = group.items[nextIdx]

        // Apply casing if all items are lowercase
        if (group.items.all { it.all { c -> c.isLowerCase() || !c.isLetter() } }) {
            val detected = Casing.detect(original)
            return Casing.apply(nextItem, detected)
        }

        return nextItem
    }

    private fun List<String>.indexOfAny(word: String): Int {
        // First try case-sensitive match
        val exactIdx = this.indexOf(word)
        if (exactIdx >= 0) return exactIdx

        // Then try case-insensitive if the list is all lowercase
        if (this.all { it.all { c -> c.isLowerCase() || !c.isLetter() } }) {
            return this.indexOfFirst { it.equals(word, ignoreCase = true) }
        }

        return -1
    }
}
