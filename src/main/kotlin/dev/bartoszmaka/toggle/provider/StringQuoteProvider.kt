package dev.bartoszmaka.toggle.provider

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil

class StringQuoteProvider : ToggleProvider {

    override fun findToggle(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): ToggleMatch? {
        val leaf = file.findElementAt(caretOffset) ?: return null
        val literal = PsiTreeUtil.getParentOfType(leaf, PsiLanguageInjectionHost::class.java, false)
            ?: return null

        val range = literal.textRange
        if (caretOffset !in range.startOffset until range.endOffset) return null

        val text = literal.text
        val quoteSet = quoteSetFor(file.language.id) ?: return null
        val (currentDelim, content) = parseDelimiter(text, quoteSet) ?: return null
        val nextDelim = nextDelimiter(currentDelim, quoteSet) ?: return null
        val newContent = transformContent(content, currentDelim, nextDelim)

        return ToggleMatch(
            range = range,
            replacement = nextDelim + newContent + nextDelim,
        )
    }

    internal fun quoteSetFor(languageId: String): List<String>? = when (languageId) {
        "ruby" -> listOf("\"", "'")
        "Python" -> listOf("\"", "'")
        "JavaScript", "TypeScript", "ECMAScript 6" -> listOf("\"", "'", "`")
        "kotlin" -> listOf("\"", "\"\"\"")
        "JAVA" -> null
        else -> listOf("\"", "'")
    }

    internal fun parseDelimiter(text: String, quoteSet: List<String>): Pair<String, String>? {
        val byLength = quoteSet.sortedByDescending { it.length }
        for (delim in byLength) {
            if (text.length >= 2 * delim.length &&
                text.startsWith(delim) &&
                text.endsWith(delim)
            ) {
                return delim to text.substring(delim.length, text.length - delim.length)
            }
        }
        return null
    }

    internal fun nextDelimiter(current: String, quoteSet: List<String>): String? {
        val idx = quoteSet.indexOf(current)
        if (idx < 0 || quoteSet.size < 2) return null
        return quoteSet[(idx + 1) % quoteSet.size]
    }

    internal fun transformContent(content: String, oldDelim: String, newDelim: String): String {
        if (oldDelim.length != 1 || newDelim.length != 1) return content

        val oldCh = oldDelim[0]
        val newCh = newDelim[0]
        val out = StringBuilder(content.length + 8)
        var i = 0
        while (i < content.length) {
            val c = content[i]
            if (c == '\\' && i + 1 < content.length) {
                val next = content[i + 1]
                if (next == oldCh) {
                    out.append(next)
                } else {
                    out.append(c).append(next)
                }
                i += 2
            } else if (c == newCh) {
                out.append('\\').append(c)
                i++
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }
}
