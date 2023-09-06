package ui

import ui.model.InstrumentPickerUiModel
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel

class NewTickerScreen(
    private val onAddTickerRequested: (InstrumentPickerUiModel) -> Unit
) {
    private val newTickerPanel = JPanel()
    private val newTickerButton = JButton("Add Ticker")
    private val newTickerComboBox = JComboBox<InstrumentPickerUiModel>()
    private val newTickerLayout = GridBagLayout()

    val component: Component = newTickerPanel
    fun setAvailableInstruments(instruments: List<InstrumentPickerUiModel>) {
        newTickerComboBox.removeAllItems()
        instruments.forEach {
            newTickerComboBox.addItem(it)
        }
        if (instruments.isNotEmpty()) {
            newTickerButton.isEnabled = true
            newTickerComboBox.isEnabled = true
            newTickerComboBox.selectedItem = instruments.first()
        }
        newTickerPanel.revalidate()
    }

    fun initUi() {
        newTickerPanel.layout = newTickerLayout

        newTickerPanel.add(newTickerButton, GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
        })
        newTickerPanel.add(newTickerComboBox, GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
        })

        newTickerComboBox.addItem(LOADING_ITEM)
        newTickerComboBox.isEnabled = false

        newTickerButton.maximumSize = Dimension(newTickerButton.width, Int.MAX_VALUE)
        newTickerButton.alignmentY = Component.CENTER_ALIGNMENT
        newTickerButton.alignmentX = Component.CENTER_ALIGNMENT
        newTickerButton.isEnabled = false

        newTickerButton.addActionListener {
            (newTickerComboBox.selectedItem as? InstrumentPickerUiModel)?.let(onAddTickerRequested)
        }
    }

    companion object {
        private val LOADING_ITEM = InstrumentPickerUiModel(
            "Loading instruments...", "",
            "",
            "",
            0
        )
    }
}