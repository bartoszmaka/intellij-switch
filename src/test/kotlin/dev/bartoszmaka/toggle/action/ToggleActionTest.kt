package dev.bartoszmaka.toggle.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ToggleActionTest : BasePlatformTestCase() {

    private fun performAction() {
        val action = ActionManager.getInstance()
            .getAction("dev.bartoszmaka.toggle.ToggleAction")
            ?: error("ToggleAction not registered")
        val context = SimpleDataContext.builder()
            .add(CommonDataKeys.EDITOR, myFixture.editor)
            .add(CommonDataKeys.PSI_FILE, myFixture.file)
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val event = AnActionEvent.createFromAnAction(action, null, "test", context)
        action.actionPerformed(event)
    }

    fun testTogglesBooleanInJavaFile() {
        myFixture.configureByText(
            "A.java",
            "class A { boolean b = tr<caret>ue; }",
        )
        performAction()
        assertEquals("class A { boolean b = false; }", myFixture.editor.document.text)
    }

    fun testTogglesCharInJavaFile() {
        myFixture.configureByText(
            "A.java",
            "class A { int x = 1 <caret>+ 2; }",
        )
        performAction()
        assertEquals("class A { int x = 1 - 2; }", myFixture.editor.document.text)
    }

    fun testTogglesKotlinStringQuoteStyle() {
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
