package com.humbledroid.kommander

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class KustomCommandsToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = KustomCommandsPanel(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}