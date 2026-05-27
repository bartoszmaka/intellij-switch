package dev.bartoszmaka.switch.provider

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil

data class StringForm(val prefix: String, val suffix: String)

class StringQuoteProvider : SwitchProvider {

    override fun findSwitch(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        rules: EffectiveRules,
        reverse: Boolean,
    ): SwitchMatch? {
        val forms = formsFor(file.language.id) ?: return null
        if (forms.size < 2) return null

        val current = findCurrentLiteral(file, editor, caretOffset, forms) ?: return null
        val dir = if (reverse) -1 else 1
        val nextIdx = ((current.formIdx + dir) % forms.size + forms.size) % forms.size
        val oldForm = forms[current.formIdx]
        val newForm = forms[nextIdx]
        val newContent = transformContent(current.content, oldForm, newForm)

        return SwitchMatch(
            range = current.range,
            replacement = newForm.prefix + newContent + newForm.suffix,
        )
    }

    internal fun formsFor(languageId: String): List<StringForm>? = when (languageId) {
        "ruby" -> listOf(
            StringForm("'", "'"),
            StringForm("\"", "\""),
            StringForm(":", ""),
        )
        "Python" -> listOf(StringForm("\"", "\""), StringForm("'", "'"))
        "JavaScript", "TypeScript", "ECMAScript 6" -> listOf(
            StringForm("\"", "\""),
            StringForm("'", "'"),
            StringForm("`", "`"),
        )
        "kotlin" -> listOf(StringForm("\"", "\""), StringForm("\"\"\"", "\"\"\""))
        "JAVA" -> null
        else -> listOf(StringForm("\"", "\""), StringForm("'", "'"))
    }

    internal data class CurrentLiteral(val range: TextRange, val formIdx: Int, val content: String)

    private fun findCurrentLiteral(
        file: PsiFile,
        editor: Editor,
        caretOffset: Int,
        forms: List<StringForm>,
    ): CurrentLiteral? {
        psiLiteralAt(file, caretOffset, forms)?.let { return it }
        if (file.language.id == "ruby") {
            findRubySymbolInText(editor.document.text, caretOffset, forms)?.let { return it }
        }
        return null
    }

    private fun psiLiteralAt(file: PsiFile, caretOffset: Int, forms: List<StringForm>): CurrentLiteral? {
        val leaf = file.findElementAt(caretOffset) ?: return null
        val literal = PsiTreeUtil.getParentOfType(leaf, PsiLanguageInjectionHost::class.java, false)
            ?: return null
        val range = literal.textRange
        if (caretOffset !in range.startOffset until range.endOffset) return null

        val (formIdx, content) = parseForm(literal.text, forms) ?: return null
        return CurrentLiteral(range, formIdx, content)
    }

    internal fun parseForm(text: String, forms: List<StringForm>): Pair<Int, String>? {
        val ordered = forms
            .mapIndexed { idx, form -> idx to form }
            .sortedByDescending { it.second.prefix.length + it.second.suffix.length }
        for ((idx, form) in ordered) {
            val minLen = form.prefix.length + form.suffix.length
            if (text.length < minLen) continue
            if (form.suffix.isEmpty()) {
                // Prefix-only form (e.g. Ruby symbol): match identifier body after the prefix.
                if (!text.startsWith(form.prefix)) continue
                val body = text.substring(form.prefix.length)
                if (body.isEmpty() || !isRubyIdentifier(body)) continue
                return idx to body
            }
            if (text.startsWith(form.prefix) && text.endsWith(form.suffix)) {
                val content = text.substring(form.prefix.length, text.length - form.suffix.length)
                return idx to content
            }
        }
        return null
    }

    internal fun transformContent(content: String, old: StringForm, new: StringForm): String {
        // Only rewrite escapes when both forms are single-char string delimiters
        // (i.e. matched prefix == suffix and length 1). Symbol form has empty suffix
        // and isn't a string body, so we don't touch escapes for it.
        if (old.prefix.length != 1 || old.prefix != old.suffix) return content
        if (new.prefix.length != 1 || new.prefix != new.suffix) return content

        val oldCh = old.prefix[0]
        val newCh = new.prefix[0]
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

    companion object {
        private fun isIdentStart(c: Char) = c == '_' || c in 'A'..'Z' || c in 'a'..'z'
        private fun isIdentPart(c: Char) = isIdentStart(c) || c in '0'..'9'

        internal fun isRubyIdentifier(s: String): Boolean {
            if (s.isEmpty()) return false
            if (!isIdentStart(s[0])) return false
            for (i in 1 until s.length) {
                if (!isIdentPart(s[i])) return false
            }
            return true
        }

        internal fun findRubySymbolInText(
            text: String,
            caretOffset: Int,
            forms: List<StringForm>,
        ): CurrentLiteral? {
            val symbolFormIdx = forms.indexOfFirst { it.prefix == ":" && it.suffix.isEmpty() }
            if (symbolFormIdx < 0) return null

            // Case A: caret sits on (or after) an identifier preceded by a single `:`.
            val identStart = findIdentStart(text, caretOffset)
            if (identStart != null) {
                val (idStart, idEnd) = identStart
                if (idStart >= 1 && text[idStart - 1] == ':' &&
                    (idStart < 2 || text[idStart - 2] != ':')
                ) {
                    return CurrentLiteral(
                        range = TextRange(idStart - 1, idEnd),
                        formIdx = symbolFormIdx,
                        content = text.substring(idStart, idEnd),
                    )
                }
            }

            // Case B: caret is directly on a `:` that begins a symbol.
            if (caretOffset in text.indices && text[caretOffset] == ':') {
                val isScopeRes = (caretOffset + 1 < text.length && text[caretOffset + 1] == ':') ||
                    (caretOffset > 0 && text[caretOffset - 1] == ':')
                if (!isScopeRes &&
                    caretOffset + 1 < text.length && isIdentStart(text[caretOffset + 1])
                ) {
                    var end = caretOffset + 1
                    while (end < text.length && isIdentPart(text[end])) end++
                    return CurrentLiteral(
                        range = TextRange(caretOffset, end),
                        formIdx = symbolFormIdx,
                        content = text.substring(caretOffset + 1, end),
                    )
                }
            }

            return null
        }

        private fun findIdentStart(text: String, caretOffset: Int): Pair<Int, Int>? {
            val n = text.length
            val onChar = caretOffset in 0 until n && isIdentPart(text[caretOffset])
            val behindChar = caretOffset > 0 && caretOffset <= n && isIdentPart(text[caretOffset - 1])
            if (!onChar && !behindChar) return null
            val anchor = if (onChar) caretOffset else caretOffset - 1
            var start = anchor
            while (start > 0 && isIdentPart(text[start - 1])) start--
            var end = anchor
            while (end < n && isIdentPart(text[end])) end++
            if (!isIdentStart(text[start])) return null
            return start to end
        }
    }
}
