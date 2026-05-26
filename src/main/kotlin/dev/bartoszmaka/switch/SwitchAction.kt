package dev.bartoszmaka.switch

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import dev.bartoszmaka.switch.provider.CharSwitchProvider
import dev.bartoszmaka.switch.provider.StringQuoteProvider
import dev.bartoszmaka.switch.provider.SwitchMatch
import dev.bartoszmaka.switch.provider.SwitchProvider
import dev.bartoszmaka.switch.provider.WordSwitchProvider
import dev.bartoszmaka.switch.settings.SwitchSettings

class SwitchAction : AnAction() {

    private val providers: List<SwitchProvider> = listOf(
        StringQuoteProvider(),
        WordSwitchProvider(),
        CharSwitchProvider(),
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
        val rules = SwitchSettings.getInstance().effectiveRulesFor(file.language.id)

        val match: SwitchMatch = providers.firstNotNullOfOrNull { provider ->
            runCatching { provider.findSwitch(file, editor, caretOffset, rules) }
                .onFailure { thisLogger().warn("Switch provider ${provider::class.simpleName} failed", it) }
                .getOrNull()
        } ?: return

        WriteCommandAction.runWriteCommandAction(project, "Switch", null, {
            editor.document.replaceString(
                match.range.startOffset,
                match.range.endOffset,
                match.replacement,
            )
        })
    }
}
