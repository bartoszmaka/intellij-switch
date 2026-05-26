package dev.bartoszmaka.switch.provider

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class StringQuoteProviderTest : BasePlatformTestCase() {

    private val provider = StringQuoteProvider()

    private fun runSwitch(filename: String, before: String, expected: String) {
        myFixture.configureByText(filename, before)
        val match = provider.findSwitch(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(),
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

    fun testTransformEscapesNewSingleDelimiter() {
        assertEquals("it\\'s", provider.transformContent("it's", "\"", "'"))
    }

    fun testTransformPreservesNewlineEscape() {
        assertEquals("line\\nbreak", provider.transformContent("line\\nbreak", "\"", "'"))
    }

    fun testTransformPreservesBackslashEscape() {
        assertEquals("path\\\\to\\\\file", provider.transformContent("path\\\\to\\\\file", "\"", "'"))
    }

    fun testTransformPreservesUnicodeEscape() {
        assertEquals("u\\u0041", provider.transformContent("u\\u0041", "\"", "'"))
    }

    fun testTransformIsNoOpForMultiCharDelim() {
        assertEquals("has \" inside", provider.transformContent("has \" inside", "\"", "\"\"\""))
    }
}
