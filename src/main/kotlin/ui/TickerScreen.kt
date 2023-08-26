package ui

import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingConstants

class TickerScreen(
    val symbol: String,
    val onDeactivateClickListener: (symbol: String) -> Unit
) {

    private val box = Box.createVerticalBox()
    private val deactivateButton = JButton()
    private val tickerSymbolLabel = JLabel("", SwingConstants.CENTER)
    private val tickerPriceLabel = JLabel("", SwingConstants.CENTER)

    val component: Component = box

    init {
        box.setSize(200, 200)
        box.minimumSize = Dimension(200, 200)
        box.add(Box.createVerticalGlue())
        box.add(tickerSymbolLabel)
        box.add(tickerPriceLabel)
        box.add(Box.createVerticalGlue())
        box.add(deactivateButton)

        tickerSymbolLabel.font = Font("Arial", 0, 30)//TODO system font
        tickerSymbolLabel.text = symbol
        tickerSymbolLabel.alignmentX = Component.CENTER_ALIGNMENT

        tickerPriceLabel.font = Font("Arial", 0, 40)
        tickerPriceLabel.text = "-"
        tickerPriceLabel.alignmentX = Component.CENTER_ALIGNMENT

        deactivateButton.text = "Deactivate"
        deactivateButton.alignmentX = Component.CENTER_ALIGNMENT

        deactivateButton.addActionListener {
            onDeactivateClickListener(symbol)
        }
    }

    fun setPrice(price: String) {
        tickerPriceLabel.text = price
    }
}