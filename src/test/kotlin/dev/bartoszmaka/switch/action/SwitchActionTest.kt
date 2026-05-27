package dev.bartoszmaka.switch.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.bartoszmaka.switch.settings.GroupState
import dev.bartoszmaka.switch.settings.LanguageRulesState
import dev.bartoszmaka.switch.settings.SwitchSettings

class SwitchActionTest : BasePlatformTestCase() {

    private fun performAction(actionId: String = "dev.bartoszmaka.switch.SwitchAction") {
        val action = ActionManager.getInstance()
            .getAction(actionId)
            ?: error("$actionId not registered")
        val context = SimpleDataContext.builder()
            .add(CommonDataKeys.EDITOR, myFixture.editor)
            .add(CommonDataKeys.PSI_FILE, myFixture.file)
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val event = AnActionEvent.createFromAnAction(action, null, "test", context)
        action.actionPerformed(event)
    }

    fun testSwitchesBooleanInJavaFile() {
        myFixture.configureByText(
            "A.java",
            "class A { boolean b = tr<caret>ue; }",
        )
        performAction()
        assertEquals("class A { boolean b = false; }", myFixture.editor.document.text)
    }

    fun testSwitchesCharInJavaFile() {
        myFixture.configureByText(
            "A.java",
            "class A { int x = 1 <caret>+ 2; }",
        )
        performAction()
        assertEquals("class A { int x = 1 - 2; }", myFixture.editor.document.text)
    }

    fun testSwitchesKotlinStringQuoteStyle() {
        myFixture.configureByText(
            "a.kt",
            "val s = \"hel<caret>lo\"",
        )
        performAction()
        assertEquals("val s = \"\"\"hello\"\"\"", myFixture.editor.document.text)
    }

    fun testNoOpWhenNothingMatches() {
        myFixture.configureByText("A.java", "class A { String x = ban<caret>ana; }")
        performAction()
        assertEquals("class A { String x = banana; }", myFixture.editor.document.text)
    }

    fun testBackwardsActionIsRegistered() {
        val action = ActionManager.getInstance()
            .getAction("dev.bartoszmaka.switch.SwitchBackwardsAction")
        assertNotNull("SwitchBackwardsAction must be registered in plugin.xml", action)
    }

    fun testBackwardsThreeCycleStepsInReverse() {
        // Inject a 3-cycle global group so we can confirm the backwards action
        // truly dispatches with reverse=true (a 2-cycle gives the same neighbor
        // in both directions and wouldn't tell us anything).
        val settings = SwitchSettings.getInstance()
        settings.loadState(SwitchSettings.State().apply {
            global = LanguageRulesState(
                wordGroups = mutableListOf(GroupState(mutableListOf("alpha", "beta", "gamma"))),
                charGroups = mutableListOf(),
                inheritsGlobal = false,
            )
        })
        try {
            myFixture.configureByText(
                "A.java",
                "class A { String s = al<caret>pha; }",
            )
            performAction("dev.bartoszmaka.switch.SwitchBackwardsAction")
            assertEquals("class A { String s = gamma; }", myFixture.editor.document.text)
        } finally {
            // Restore default settings — capturing settings.state before mutation
            // would only capture a reference (loadState mutates in place), so
            // reload defaults explicitly.
            settings.loadState(SwitchSettings.State())
        }
    }

    fun testSingleUndoStep() {
        myFixture.configureByText(
            "A.java",
            "class A { boolean b = tr<caret>ue; }",
        )
        performAction()
        assertEquals("class A { boolean b = false; }", myFixture.editor.document.text)
        val fileEditor = FileEditorManager.getInstance(project)
            .getSelectedEditor(myFixture.file.virtualFile)
            ?: error("File editor should exist after configureByText")
        val undo = UndoManager.getInstance(project)
        assertTrue("Undo should be available", undo.isUndoAvailable(fileEditor))
        undo.undo(fileEditor)
        assertEquals("class A { boolean b = true; }", myFixture.editor.document.text)
    }
}
