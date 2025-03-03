package com.humbledroid.kommander

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class AddKommandDialog(
    private val project: Project,
    private val originalItem: KustomCommandState.CommandItem?
) : DialogWrapper(project, true) {
    
    private lateinit var nameField: JTextField
    private lateinit var commandField: JTextField
    private lateinit var workingDirField: TextFieldWithBrowseButton
    
    init {
        title = if (originalItem == null) "Add Command" else "Edit Command"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = JBUI.insets(5)

        gbc.gridx = 0
        gbc.gridy = 0
        dialogPanel.add(JLabel("Name:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        nameField = JTextField()
        originalItem?.let {
            nameField.text = it.name
        }
        dialogPanel.add(nameField, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        dialogPanel.add(JLabel("Command:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        commandField = JTextField()
        originalItem?.let {
            commandField.text = it.command
        }
        dialogPanel.add(commandField, gbc)

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weightx = 0.0
        dialogPanel.add(JLabel("Working Directory:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        workingDirField = TextFieldWithBrowseButton()
        workingDirField.addBrowseFolderListener(
            "Select Working Directory",
            null,
            project,
            FileChooserDescriptor(false, true, false, false, false, false)
        )
        
        originalItem?.let {
            workingDirField.text = it.workingDir
        } ?: run {
            workingDirField.text = project.basePath ?: ""
        }
        
        dialogPanel.add(workingDirField, gbc)
        
        val panel = JPanel(BorderLayout())
        panel.add(dialogPanel, BorderLayout.CENTER)
        panel.preferredSize = Dimension(400, 150)
        panel.border = JBUI.Borders.empty(10)
        
        return panel
    }
    
    fun getKommandItem(): KustomCommandState.CommandItem = KustomCommandState.CommandItem(
        name = nameField.text,
        command = commandField.text,
        workingDir = workingDirField.text
    )
    
    override fun doOKAction() {
        if (nameField.text.trim().isEmpty()) {
            Messages.showErrorDialog("Command name cannot be empty", "Validation Error")
            return
        }
        
        if (commandField.text.trim().isEmpty()) {
            Messages.showErrorDialog("Command cannot be empty", "Validation Error")
            return
        }
        
        super.doOKAction()
    }
}
