package com.github.ghost3432.translateideaplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.ghost3432.translateideaplugin.MyBundle
import com.github.ghost3432.translateideaplugin.services.TranslateService
import com.intellij.openapi.application.invokeLater
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.panels.VerticalLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyListener


class TranslateToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<TranslateService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = VerticalLayout(2)

            val area1 = JBTextArea(5, 1)
            val area2 = JBTextArea(5, 1)

            add(JBLabel(MyBundle.message("english")))
            add(area1)
            add(JBLabel(MyBundle.message("russian")))
            add(area2)

            class KeyTypedListener(val source: JBTextArea, val destination: JBTextArea, val from: String, val to: String) : KeyListener
            {
                fun action()
                {
                    service.translate(source.text, from, to) {
                        invokeLater {
                            destination.text = it
                        }
                    }
                }

                override fun keyTyped(e: KeyEvent?) = action()
                override fun keyPressed(e: KeyEvent?) = action()
                override fun keyReleased(e: KeyEvent?) = action()
            }

            area1.apply {
                lineWrap = true
                addKeyListener(KeyTypedListener(this, area2, "en", "ru"))
            }

            area2.apply {
                lineWrap = true
                addKeyListener(KeyTypedListener(this, area1, "ru", "en"))
            }
        }
    }
}
