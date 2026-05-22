package dev.bartoszmaka.toggle.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost

class StringQuoteProvider : ToggleProvider {

    private val quoteMap = mapOf(
        "ruby" to listOf("\"", "'"),
        "Python" to listOf("\"", "'"),
        "JavaScript" to listOf("\"", "'", "`"),
        "TypeScript" to listOf("\"", "'", "`"),
        "kotlin" to listOf("\"", "\"\"\""),
        "JAVA" to listOf(),
    )

    override fun findToggle(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
    ): ToggleMatch? {
        val elementAtCaret = file.findElementAt(caretOffset) ?: return null

        // Walk up to find a string literal (PsiLanguageInjectionHost)
        var element = elementAtCaret
        while (element != null) {
            if (element is PsiLanguageInjectionHost) {
                val range = element.textRange
                if (caretOffset in range.startOffset until range.endOffset) {
                    return tryToggleQuotes(element.text, range, file)
                }
            }
            element = element.parent
        }

        return null
    }

    private fun tryToggleQuotes(text: String, range: TextRange, file: PsiFile): ToggleMatch? {
        val langId = file.language.id
        val quotes = quoteMap[langId] ?: listOf("\"", "'")
        if (quotes.isEmpty()) return null

        for (quote in quotes) {
            if (text.startsWith(quote) && text.endsWith(quote) && text.length > 2 * quote.length) {
                val nextIdx = (quotes.indexOf(quote) + 1) % quotes.size
                val nextQuote = quotes[nextIdx]
                val inner = text.substring(quote.length, text.length - quote.length)
                val escaped = reescapeString(inner, quote, nextQuote)
                val replacement = nextQuote + escaped + nextQuote
                return ToggleMatch(range, replacement)
            }
        }

        return null
    }

    private fun reescapeString(inner: String, oldQuote: String, newQuote: String): String {
        // Basic re-escaping: unescape old quote, escape new quote
        var result = inner.replace("\\$oldQuote", oldQuote)
        result = result.replace(newQuote, "\\$newQuote")
        return result
    }
}
