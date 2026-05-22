package dev.bartoszmaka.toggle.provider

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class WordToggleProviderTest : BasePlatformTestCase() {

    private val provider = WordToggleProvider()

    fun testDigitsPreventAllLowercaseCasingPreservation() {
        myFixture.configureByText("a.txt", "LOW<caret>1")

        val match = provider.findToggle(
            myFixture.file,
            myFixture.editor,
            myFixture.editor.caretModel.offset,
            EffectiveRules(
                wordGroups = listOf(ToggleGroup(listOf("low1", "high2"))),
            ),
        )

        assertNull(match)
    }
}
