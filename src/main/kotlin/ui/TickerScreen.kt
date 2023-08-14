package ui

import SLBottom
import SLLeft
import SLRight
import SLTop
import kotlinx.coroutines.runBlocking
import java.awt.Component
import java.awt.Font
import javax.swing.*

class TickerScreen(
    private val viewModel: TickerViewModel
) {
    private val frame: JFrame = JFrame()
    private val button: JButton = JButton()
    private val label: JLabel = JLabel("", SwingConstants.CENTER)

    init {
        runBlocking {
            initUi()

            viewModel.observePrice().collect { price ->
                label.text = price.toPlainString()
            }
        }
    }

    private fun initUi() {
        val layout = SpringLayout()
        frame.layout = layout
        val contentPane = frame.contentPane
        contentPane.add(label)
        contentPane.add(button)

        //        frame.pack()
        //        frame.contentPane = panel
        frame.setSize(300, 300)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.title = "Increment"
        frame.isVisible = true
        frame.isResizable = false

        label.font = Font("Arial", 0, 50)
        label.text = "-"
        label.alignmentX = Component.CENTER_ALIGNMENT

        button.text = "Increment"
        button.font = Font("Arial", 0, 30)
        button.alignmentX = Component.CENTER_ALIGNMENT

        button.addActionListener {
            TODO()
        }

        button.isEnabled = false

        layout.putConstraint(SLLeft, label, 8, SLLeft, contentPane)
        layout.putConstraint(SLRight, label, -8, SLRight, contentPane)
        layout.putConstraint(SLTop, label, 8, SLTop, contentPane)
        layout.putConstraint(SLBottom, label, 8, SLTop, button)
        layout.putConstraint(SLLeft, button, 8, SLLeft, contentPane)
        layout.putConstraint(SLRight, button, -8, SLRight, contentPane)
        layout.putConstraint(SLBottom, button, -8, SLBottom, contentPane)
        //        layout.putConstraint(SLTop, button, 8, SpringLayout.SOUTH, label)

        //        frame.pack()
    }


}