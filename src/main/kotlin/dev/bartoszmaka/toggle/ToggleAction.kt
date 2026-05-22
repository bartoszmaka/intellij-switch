package dev.bartoszmaka.toggle

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import dev.bartoszmaka.toggle.provider.CharToggleProvider
import dev.bartoszmaka.toggle.provider.StringQuoteProvider
import dev.bartoszmaka.toggle.provider.WordToggleProvider
import dev.bartoszmaka.toggle.settings.ToggleSettings

class ToggleAction : AnAction() {

    private val providers = listOf(
        StringQuoteProvider(),
        WordToggleProvider(),
        CharToggleProvider(),
    )

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret = editor.caretModel.primaryCaret
        val caretOffset = caret.offset

        val settings = service<ToggleSettings>()
        val rules = settings.getEffectiveRules(file.language.id)

        for (provider in providers) {
            val match = try {
                provider.findToggle(file, editor, caretOffset, rules)
            } catch (ex: Exception) {
                null
            }

            if (match != null) {
                WriteCommandAction.runWriteCommandAction(file.project) {
                    editor.document.replaceString(match.range.startOffset, match.range.endOffset, match.replacement)
                }
                return
            }
        }
    }
}
