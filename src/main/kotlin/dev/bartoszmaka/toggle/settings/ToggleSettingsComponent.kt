package dev.bartoszmaka.toggle.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import java.awt.BorderLayout
import java.awt.FlowLayout

class ToggleSettingsComponent {

    val panel: JPanel = JPanel(BorderLayout())
    private var initialized = false
    private var modified = false

    private val languages = mutableListOf("Global")
    private val languageList = JList(languages.toTypedArray())
    private val wordTable = JBTable(WordTableModel())
    private val charTable = JBTable(CharTableModel())
    private val inheritCheckbox = JCheckBox("Inherit from Global", true)

    private val settings: ToggleSettings = service()
    private var currentState: ToggleSettings.State? = null
    private var currentLang: String? = null

    init {
        buildUI()
        loadSettings()
        initialized = true
    }

    private fun buildUI() {
        val splitPane = JPanel(BorderLayout())

        // Left pane: language list
        val leftPane = JPanel(BorderLayout())
        languageList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        languageList.addListSelectionListener { loadLanguage(languageList.selectedValue) }
        leftPane.add(JBScrollPane(languageList), BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val addButton = JButton("+ Add language…")
        buttonPanel.add(addButton)
        leftPane.add(buttonPanel, BorderLayout.SOUTH)
        splitPane.add(leftPane, BorderLayout.WEST)

        // Right pane: settings tables
        val rightPane = JPanel(BorderLayout())
        rightPane.add(JBLabel("Word Groups"), BorderLayout.NORTH)
        val wordDecorated = ToolbarDecorator.createDecorator(wordTable)
            .createPanel()
        rightPane.add(wordDecorated, BorderLayout.CENTER)

        rightPane.add(JBLabel("Character Groups"), BorderLayout.SOUTH)
        val charDecorated = ToolbarDecorator.createDecorator(charTable)
            .createPanel()
        rightPane.add(charDecorated, BorderLayout.SOUTH)

        rightPane.add(inheritCheckbox, BorderLayout.SOUTH)

        splitPane.add(rightPane, BorderLayout.CENTER)
        panel.add(splitPane, BorderLayout.CENTER)
    }

    private fun loadSettings() {
        val state = settings.getState() ?: ToggleSettings.State()
        currentState = state
    }

    private fun loadLanguage(lang: String) {
        currentLang = lang
        modified = true
    }

    fun isModified(): Boolean = modified

    fun apply() {
        modified = false
    }

    fun reset() {
        loadSettings()
        modified = false
    }

    private inner class WordTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = 0
        override fun getColumnCount(): Int = 1
        override fun getColumnName(column: Int): String = "Word Groups"
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = ""
    }

    private inner class CharTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = 0
        override fun getColumnCount(): Int = 1
        override fun getColumnName(column: Int): String = "Character Groups"
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = ""
    }
}
