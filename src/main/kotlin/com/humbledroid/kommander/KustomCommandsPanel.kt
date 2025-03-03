package com.humbledroid.kommander

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class KustomCommandsPanel(private val project: Project) : SimpleToolWindowPanel(false, true) {

    private val listModel = DefaultListModel<KustomCommandState.CommandItem>()
    private val commandList: JBList<KustomCommandState.CommandItem>

    init {
        val mainPanel = JPanel(BorderLayout())
        val toolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val addButton = JButton("Add Command", null)
        addButton.addActionListener { onAddCommand() }
        toolbarPanel.add(addButton)

        loadCommandsFromState()

        commandList = JBList(listModel)
        commandList.cellRenderer = CommandItemRenderer()
        commandList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        commandList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    onRunCommand()
                }
            }
        })

        val listPanel = JPanel(BorderLayout())
        listPanel.add(JBScrollPane(commandList), BorderLayout.CENTER)

        val actionPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val editButton = JButton("Edit", null)
        val deleteButton = JButton("Delete", null)

        editButton.addActionListener { onEditCommand() }
        deleteButton.addActionListener { onDeleteCommand() }

        actionPanel.add(editButton)
        actionPanel.add(deleteButton)

        listPanel.add(actionPanel, BorderLayout.SOUTH)
        mainPanel.add(toolbarPanel, BorderLayout.NORTH)
        mainPanel.add(listPanel, BorderLayout.CENTER)
        setContent(mainPanel)
    }

    private fun loadCommandsFromState() {
        val state = KustomCommandState.getInstance(project)
        listModel.clear()
        for (item in state.getCommands()) {
            listModel.addElement(item)
        }
    }

    private fun onAddCommand() {
        val dialog = AddKommandDialog(project, null)
        if (dialog.showAndGet()) {
            val newItem = dialog.getKommandItem()
            val state = KustomCommandState.getInstance(project)
            state.addCommand(newItem)
            listModel.addElement(newItem)
        }
    }

    private fun onEditCommand() {
        val selectedIndex = commandList.selectedIndex
        if (selectedIndex >= 0) {
            val selectedItem = listModel.getElementAt(selectedIndex)

            val dialog = AddKommandDialog(project, selectedItem)
            if (dialog.showAndGet()) {
                val updatedItem = dialog.getKommandItem()
                listModel.set(selectedIndex, updatedItem)
                val state = KustomCommandState.getInstance(project)
                state.updateCommand(selectedIndex, updatedItem)
            }
        }
    }

    private fun onDeleteCommand() {
        val selectedIndex = commandList.selectedIndex
        if (selectedIndex >= 0) {
            val result = Messages.showYesNoDialog(
                "Are you sure you want to delete this command?",
                "Confirm",
                Messages.getQuestionIcon()
            )

            if (result == Messages.YES) {
                val state = KustomCommandState.getInstance(project)
                state.removeCommand(selectedIndex)
                listModel.remove(selectedIndex)
            }
        }
    }

    private fun onRunCommand() {
        val selectedIndex = commandList.selectedIndex
        if (selectedIndex >= 0) {
            val selectedItem = listModel.getElementAt(selectedIndex)
            executeCommand(selectedItem)
        }
    }

    private fun executeCommand(commandItem: KustomCommandState.CommandItem) {
        try {
            val commandParts = commandItem.command.split("\\s+".toRegex())
            val commandLine = GeneralCommandLine()
            commandLine.exePath = commandParts[0]
            if (commandParts.size > 1) {
                commandLine.addParameters(commandParts.subList(1, commandParts.size))
            }
            commandLine.withWorkDirectory(commandItem.workingDir)
            val consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .console
            val processHandler = OSProcessHandler(commandLine)
            consoleView.attachToProcess(processHandler)
            val contentManager = RunContentManager.getInstance(project)
            val descriptor = com.intellij.execution.ui.RunContentDescriptor(
                consoleView,
                processHandler,
                consoleView.component,
                commandItem.name
            )

            contentManager.showRunContent(
                com.intellij.execution.executors.DefaultRunExecutor.getRunExecutorInstance(),
                descriptor
            )
            processHandler.startNotify()
        } catch (e: Exception) {
            Messages.showErrorDialog(
                "Error executing command: ${e.message}",
                "Command Execution Error"
            )
        }
    }

    // Custom renderer for the list items
    private class CommandItemRenderer : ColoredListCellRenderer<KustomCommandState.CommandItem>() {
        override fun customizeCellRenderer(
            list: JList<out KustomCommandState.CommandItem>,
            value: KustomCommandState.CommandItem?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            value?.let {
                append(it.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                append(" (${it.command})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        }
    }
}