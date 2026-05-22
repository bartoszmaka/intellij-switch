package dev.bartoszmaka.toggle

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import dev.bartoszmaka.toggle.provider.CharToggleProvider
import dev.bartoszmaka.toggle.provider.StringQuoteProvider
import dev.bartoszmaka.toggle.provider.ToggleMatch
import dev.bartoszmaka.toggle.provider.ToggleProvider
import dev.bartoszmaka.toggle.provider.WordToggleProvider
import dev.bartoszmaka.toggle.settings.ToggleSettings

class ToggleAction : AnAction() {

    private val providers: List<ToggleProvider> = listOf(
        StringQuoteProvider(),
        WordToggleProvider(),
        CharToggleProvider(),
    )

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = editor != null && file != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = e.project ?: return

        val caretOffset = editor.caretModel.primaryCaret.offset
        val rules = ToggleSettings.getInstance().effectiveRulesFor(file.language.id)

        val match: ToggleMatch = providers.firstNotNullOfOrNull { provider ->
            runCatching { provider.findToggle(file, editor, caretOffset, rules) }
                .onFailure { thisLogger().warn("Toggle provider ${provider::class.simpleName} failed", it) }
                .getOrNull()
        } ?: return

        WriteCommandAction.runWriteCommandAction(project, "Toggle", null, {
            editor.document.replaceString(
                match.range.startOffset,
                match.range.endOffset,
                match.replacement,
            )
        })
    }
}
