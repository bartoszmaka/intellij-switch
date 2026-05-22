package dev.bartoszmaka.toggle.settings

import com.intellij.lang.Language
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ToggleSettingsComponent {

    private val state = ToggleSettings.getInstance().state
    private val working: ToggleSettings.State = copyOf(state)

    private val languageListModel = CollectionListModel<String>().apply {
        add(GLOBAL_LANGUAGE)
        working.perLanguage.keys.sorted().forEach { add(it) }
    }
    private val languageList = JBList(languageListModel).apply {
        selectedIndex = 0
        addListSelectionListener {
            if (!it.valueIsAdjusting) refreshRightPane()
        }
    }

    private val rightPane = JBPanel<JBPanel<*>>(BorderLayout())
    private val inheritCheckBox = JBCheckBox("Inherit from Global")

    private val wordGroupsModel = CollectionListModel<List<String>>()
    private val charGroupsModel = CollectionListModel<List<String>>()
    private val wordGroupsList = JBList(wordGroupsModel).apply {
        cellRenderer = GroupCellRenderer()
    }
    private val charGroupsList = JBList(charGroupsModel).apply {
        cellRenderer = GroupCellRenderer()
    }

    val panel: JComponent

    init {
        val left = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(200, 0)
            border = BorderFactory.createTitledBorder("Languages")
            add(JBScrollPane(languageList), BorderLayout.CENTER)
            add(buildLanguageToolbar(), BorderLayout.SOUTH)
        }
        panel = JPanel(BorderLayout()).apply {
            add(left, BorderLayout.WEST)
            add(rightPane, BorderLayout.CENTER)
        }
        refreshRightPane()
    }

    private fun buildLanguageToolbar(): JComponent {
        val addButton = JButton("Add language...").apply {
            addActionListener { showAddLanguagePopup() }
        }
        val removeButton = JButton("Remove").apply {
            addActionListener { removeSelectedLanguage() }
        }
        return Box.createHorizontalBox().apply {
            add(addButton)
            add(removeButton)
            add(Box.createHorizontalGlue())
        }
    }

    private fun showAddLanguagePopup() {
        val existing = working.perLanguage.keys
        val candidates = Language.getRegisteredLanguages()
            .map { it.id }
            .filter { it.isNotEmpty() && it != GLOBAL_LANGUAGE && it !in existing }
            .sorted()
        val popup = JBPopupFactory.getInstance()
            .createPopupChooserBuilder(candidates)
            .setTitle("Add Language")
            .setItemChosenCallback { id ->
                working.perLanguage[id] = LanguageRulesState(
                    wordGroups = mutableListOf(),
                    charGroups = mutableListOf(),
                    inheritsGlobal = true,
                )
                languageListModel.add(id)
                languageList.setSelectedValue(id, true)
            }
            .createPopup()
        popup.showInBestPositionFor(languageList)
    }

    private fun removeSelectedLanguage() {
        val selected = languageList.selectedValue ?: return
        if (selected == GLOBAL_LANGUAGE) return
        working.perLanguage.remove(selected)
        languageListModel.remove(selected)
        languageList.selectedIndex = 0
        refreshRightPane()
    }

    private fun refreshRightPane() {
        rightPane.removeAll()
        val selected = languageList.selectedValue ?: GLOBAL_LANGUAGE
        val rules = if (selected == GLOBAL_LANGUAGE) {
            working.global
        } else {
            working.perLanguage.getOrPut(selected) {
                LanguageRulesState(mutableListOf(), mutableListOf(), inheritsGlobal = true)
            }
        }

        wordGroupsModel.removeAll()
        rules.wordGroups.forEach { wordGroupsModel.add(it.items.toList()) }
        charGroupsModel.removeAll()
        rules.charGroups.forEach { charGroupsModel.add(it.items.toList()) }

        val inheritPanel = Box.createHorizontalBox().apply {
            border = BorderFactory.createEmptyBorder(6, 6, 6, 6)
            if (selected == GLOBAL_LANGUAGE) {
                add(JBLabel("Default rules applied to all languages."))
            } else {
                inheritCheckBox.isSelected = rules.inheritsGlobal
                inheritCheckBox.actionListeners.forEach { inheritCheckBox.removeActionListener(it) }
                inheritCheckBox.addActionListener {
                    rules.inheritsGlobal = inheritCheckBox.isSelected
                }
                add(inheritCheckBox)
            }
            add(Box.createHorizontalGlue())
        }

        val stacked = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(inheritPanel)
            add(buildGroupSection("Word groups", wordGroupsList, wordGroupsModel, true, rules))
            add(buildGroupSection("Character groups", charGroupsList, charGroupsModel, false, rules))
        }
        rightPane.add(stacked, BorderLayout.CENTER)
        rightPane.revalidate()
        rightPane.repaint()
    }

    private fun buildGroupSection(
        title: String,
        list: JBList<List<String>>,
        model: CollectionListModel<List<String>>,
        isWord: Boolean,
        rules: LanguageRulesState,
    ): JComponent {
        val decorator = ToolbarDecorator.createDecorator(list)
            .setAddAction { showEditDialog(null, isWord) { appendGroup(it, model, rules, isWord) } }
            .setRemoveAction {
                val index = list.selectedIndex
                if (index >= 0) {
                    model.remove(index)
                    if (isWord) rules.wordGroups.removeAt(index) else rules.charGroups.removeAt(index)
                }
            }
            .setEditAction {
                val index = list.selectedIndex
                if (index >= 0) {
                    showEditDialog(model.getElementAt(index), isWord) { items ->
                        model.setElementAt(items, index)
                        val groupState = GroupState(items.toMutableList())
                        if (isWord) {
                            rules.wordGroups[index] = groupState
                        } else {
                            rules.charGroups[index] = groupState
                        }
                    }
                }
            }
        return JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder(title)
            add(decorator.createPanel(), BorderLayout.CENTER)
        }
    }

    private fun appendGroup(
        items: List<String>,
        model: CollectionListModel<List<String>>,
        rules: LanguageRulesState,
        isWord: Boolean,
    ) {
        model.add(items)
        val groupState = GroupState(items.toMutableList())
        if (isWord) rules.wordGroups.add(groupState) else rules.charGroups.add(groupState)
    }

    private fun showEditDialog(initial: List<String>?, isWord: Boolean, onAccept: (List<String>) -> Unit) {
        val dialog = GroupEditDialog(initial.orEmpty(), isWord)
        if (dialog.showAndGet()) onAccept(dialog.result)
    }

    fun isModified(): Boolean = !state.contentEquals(working)

    fun apply() {
        state.global = copyLR(working.global)
        state.perLanguage = working.perLanguage
            .mapValues { copyLR(it.value) }
            .toMutableMap()
    }

    fun reset() {
        val fresh = copyOf(state)
        working.global = fresh.global
        working.perLanguage = fresh.perLanguage
        rebuildLanguageList()
    }

    fun restoreDefaults() {
        working.global = ToggleSettings.defaultGlobalState()
        working.perLanguage = ToggleSettings.defaultPerLanguageState()
        rebuildLanguageList()
    }

    private fun rebuildLanguageList() {
        languageListModel.removeAll()
        languageListModel.add(GLOBAL_LANGUAGE)
        working.perLanguage.keys.sorted().forEach { languageListModel.add(it) }
        languageList.selectedIndex = 0
        refreshRightPane()
    }

    private fun copyOf(s: ToggleSettings.State): ToggleSettings.State {
        val out = ToggleSettings.State()
        out.global = copyLR(s.global)
        out.perLanguage = s.perLanguage
            .mapValues { copyLR(it.value) }
            .toMutableMap()
        return out
    }

    private fun copyLR(s: LanguageRulesState): LanguageRulesState = LanguageRulesState(
        wordGroups = s.wordGroups.map { GroupState(it.items.toMutableList()) }.toMutableList(),
        charGroups = s.charGroups.map { GroupState(it.items.toMutableList()) }.toMutableList(),
        inheritsGlobal = s.inheritsGlobal,
    )

    private fun ToggleSettings.State.contentEquals(other: ToggleSettings.State): Boolean {
        if (!global.contentEquals(other.global)) return false
        if (perLanguage.keys != other.perLanguage.keys) return false
        for ((language, rules) in perLanguage) {
            val otherRules = other.perLanguage[language] ?: return false
            if (!rules.contentEquals(otherRules)) return false
        }
        return true
    }

    private fun LanguageRulesState.contentEquals(other: LanguageRulesState): Boolean =
        inheritsGlobal == other.inheritsGlobal &&
            wordGroups.map { it.items } == other.wordGroups.map { it.items } &&
            charGroups.map { it.items } == other.charGroups.map { it.items }

    private class GroupCellRenderer : javax.swing.ListCellRenderer<List<String>> {
        private val delegate = DefaultListCellRenderer()

        override fun getListCellRendererComponent(
            list: JList<out List<String>>?,
            value: List<String>?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): java.awt.Component {
            val text = value?.joinToString(", ") ?: ""
            return delegate.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus)
        }
    }

    private class GroupEditDialog(initial: List<String>, private val isWord: Boolean) : DialogWrapper(true) {
        private val area = JTextArea(initial.joinToString("\n"), 10, 30)
        lateinit var result: List<String>

        init {
            title = if (isWord) "Edit Word Group" else "Edit Character Group"
            init()
            area.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = startTrackingValidation()
                override fun removeUpdate(e: DocumentEvent) = startTrackingValidation()
                override fun changedUpdate(e: DocumentEvent) = startTrackingValidation()
            })
            startTrackingValidation()
        }

        override fun createCenterPanel(): JComponent {
            val label = if (isWord) {
                "One identifier per line (e.g. true, false). Min 2 items."
            } else {
                "One single-character item per line. Min 2 items."
            }
            return JPanel(BorderLayout()).apply {
                add(JBLabel(label), BorderLayout.NORTH)
                add(JBScrollPane(area), BorderLayout.CENTER)
            }
        }

        override fun doValidate(): ValidationInfo? {
            val items = parsedItems()
            val errors = if (isWord) {
                ToggleGroupValidation.validateWordGroup(items)
            } else {
                ToggleGroupValidation.validateCharGroup(items)
            }
            return if (errors.isEmpty()) null else ValidationInfo(errors.joinToString("; "), area)
        }

        override fun doOKAction() {
            result = parsedItems()
            super.doOKAction()
        }

        private fun parsedItems(): List<String> =
            area.text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    private companion object {
        const val GLOBAL_LANGUAGE = "Global"
    }
}
