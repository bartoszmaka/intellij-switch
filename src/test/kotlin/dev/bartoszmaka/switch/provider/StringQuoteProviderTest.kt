package dev.bartoszmaka.switch.provider

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class StringQuoteProviderTest : BasePlatformTestCase() {

    private val provider = StringQuoteProvider()

    private val singleQuote = StringForm("'", "'")
    private val doubleQuote = StringForm("\"", "\"")

    private fun runSwitch(filename: String, before: String, expected: String, reverse: Boolean = false) {
        myFixture.configureByText(filename, before)
        val match = provider.findSwitch(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(),
            reverse,
        )
        assertNotNull("provider returned null", match)
        val switch = match!!
        WriteCommandAction.runWriteCommandAction(project) {
            myFixture.editor.document.replaceString(
                switch.range.startOffset,
                switch.range.endOffset,
                switch.replacement,
            )
        }
        assertEquals(expected, myFixture.editor.document.text)
    }

    fun testJavaFallbackReturnsNull() {
        myFixture.configureByText(
            "A.java",
            "class A { String s = \"he<caret>llo\"; }",
        )
        val match = provider.findSwitch(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(),
        )
        assertNull(match)
    }

    fun testPlainTextReturnsNull() {
        myFixture.configureByText("a.txt", "\"he<caret>llo\"")
        val match = provider.findSwitch(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(),
        )
        assertNull(match)
    }

    fun testCaretAfterClosingQuoteFallsThrough() {
        myFixture.configureByText(
            "A.java",
            "class A { String s = \"hello\"<caret>; }",
        )
        val match = provider.findSwitch(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(),
        )
        assertNull(match)
    }

    fun testKotlinDoubleToTriple() {
        runSwitch(
            "a.kt",
            "val s = \"hel<caret>lo\"",
            "val s = \"\"\"hello\"\"\"",
        )
    }

    fun testKotlinTripleToDouble() {
        runSwitch(
            "a.kt",
            "val s = \"\"\"hel<caret>lo\"\"\"",
            "val s = \"hello\"",
        )
    }

    fun testKotlinCaretOnOpeningQuoteTriggers() {
        runSwitch(
            "a.kt",
            "val s = <caret>\"hello\"",
            "val s = \"\"\"hello\"\"\"",
        )
    }

    fun testKotlinCaretOnClosingQuoteTriggers() {
        // <caret> marker sits between `o` and `"`, which after fixture parsing places the
        // caret offset exactly on the closing quote — i.e. range.endOffset - 1. Per the
        // spec, this should trigger. (Earlier plan added +1 which moved past the closing
        // quote into the "falls through" region; corrected here.)
        myFixture.configureByText("a.kt", "val s = \"hello<caret>\"")
        val match = provider.findSwitch(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(),
        )
        assertNotNull(match)
    }

    fun testKotlinReverseTripleToDouble() {
        // Kotlin only has a 2-form cycle, so reverse should also flip between the two forms.
        runSwitch(
            "a.kt",
            "val s = \"\"\"hel<caret>lo\"\"\"",
            "val s = \"hello\"",
            reverse = true,
        )
    }

    fun testTransformEscapesNewSingleDelimiter() {
        assertEquals("it\\'s", provider.transformContent("it's", doubleQuote, singleQuote))
    }

    fun testTransformPreservesNewlineEscape() {
        assertEquals("line\\nbreak", provider.transformContent("line\\nbreak", doubleQuote, singleQuote))
    }

    fun testTransformPreservesBackslashEscape() {
        assertEquals("path\\\\to\\\\file", provider.transformContent("path\\\\to\\\\file", doubleQuote, singleQuote))
    }

    fun testTransformPreservesUnicodeEscape() {
        assertEquals("u\\u0041", provider.transformContent("u\\u0041", doubleQuote, singleQuote))
    }

    fun testTransformIsNoOpForMultiCharDelim() {
        val triple = StringForm("\"\"\"", "\"\"\"")
        assertEquals("has \" inside", provider.transformContent("has \" inside", doubleQuote, triple))
    }

    fun testTransformIsNoOpForSymbolForm() {
        val symbol = StringForm(":", "")
        assertEquals("foo", provider.transformContent("foo", doubleQuote, symbol))
        assertEquals("foo", provider.transformContent("foo", symbol, doubleQuote))
    }

    fun testRubyFormsIncludeSymbol() {
        val forms = provider.formsFor("ruby")
        assertNotNull(forms)
        assertEquals(
            listOf(
                StringForm("'", "'"),
                StringForm("\"", "\""),
                StringForm(":", ""),
            ),
            forms,
        )
    }

    fun testParseFormSymbolMatch() {
        val forms = provider.formsFor("ruby")!!
        val parsed = provider.parseForm(":foo", forms)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.first)
        assertEquals("foo", parsed.second)
    }

    fun testParseFormRejectsSymbolWithNonIdentifierBody() {
        val forms = provider.formsFor("ruby")!!
        // ":" only matches if body is a valid identifier.
        assertNull(provider.parseForm(":\"foo bar\"", forms))
    }

    fun testParseFormPrefersStringOverSymbolWhenAmbiguous() {
        val forms = provider.formsFor("ruby")!!
        // Quoted strings always win because parseForm sorts by combined delim length desc.
        val parsed = provider.parseForm("\"foo\"", forms)
        assertEquals(1, parsed!!.first)
        assertEquals("foo", parsed.second)
    }

    fun testFindRubySymbolInTextCaretOnIdent() {
        val forms = provider.formsFor("ruby")!!
        // "x = :foo" — positions: x=0, ' '=1, '='=2, ' '=3, ':'=4, f=5, o=6, o=7
        val text = "x = :foo"
        val result = StringQuoteProvider.findRubySymbolInText(text, 6, forms)
        assertNotNull(result)
        assertEquals("foo", result!!.content)
        assertEquals(2, result.formIdx)
        assertEquals(4, result.range.startOffset) // index of ':'
        assertEquals(8, result.range.endOffset)   // end of 'foo'
    }

    fun testFindRubySymbolInTextCaretOnColon() {
        val forms = provider.formsFor("ruby")!!
        val text = "x = :foo"
        val caret = text.indexOf(':')
        val result = StringQuoteProvider.findRubySymbolInText(text, caret, forms)
        assertNotNull(result)
        assertEquals("foo", result!!.content)
        assertEquals(caret, result.range.startOffset)
    }

    fun testFindRubySymbolInTextRejectsScopeResolution() {
        val forms = provider.formsFor("ruby")!!
        // `::Foo` — leading `::` is scope resolution, not a symbol.
        val text = "::Foo"
        val caret = text.indexOf('F')
        assertNull(StringQuoteProvider.findRubySymbolInText(text, caret, forms))
    }

    fun testFindRubySymbolInTextRejectsHashShorthandValue() {
        val forms = provider.formsFor("ruby")!!
        // `key: value` — `value` is not a symbol; `:` is followed by space.
        val text = "key: value"
        val caret = text.indexOf('v')
        assertNull(StringQuoteProvider.findRubySymbolInText(text, caret, forms))
    }

    fun testFindRubySymbolInTextRejectsBareIdent() {
        val forms = provider.formsFor("ruby")!!
        val text = "foo + bar"
        val caret = text.indexOf('f')
        assertNull(StringQuoteProvider.findRubySymbolInText(text, caret, forms))
    }

}
