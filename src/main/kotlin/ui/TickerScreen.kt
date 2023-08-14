package ui

import java.awt.Component
import java.awt.Font
import java.math.BigDecimal
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
        box.add(Box.createVerticalGlue())
        box.add(tickerSymbolLabel)
        box.add(tickerPriceLabel)
        box.add(Box.createVerticalGlue())
        box.add(deactivateButton)

        tickerSymbolLabel.font = Font("Arial", 0, 30)//TODO system font
        tickerSymbolLabel.text = symbol
        tickerSymbolLabel.alignmentX = Component.CENTER_ALIGNMENT

        tickerPriceLabel.font = Font("Arial", 0, 50)
        tickerPriceLabel.text = "-"
        tickerPriceLabel.alignmentX = Component.CENTER_ALIGNMENT

        deactivateButton.text = "Deactivate"
        deactivateButton.font = Font("Arial", 0, 30)
        deactivateButton.alignmentX = Component.CENTER_ALIGNMENT

        deactivateButton.addActionListener {
            onDeactivateClickListener(symbol)
        }
    }

    fun setPrice(price: BigDecimal) {
        tickerPriceLabel.text = price.toPlainString()
    }
}