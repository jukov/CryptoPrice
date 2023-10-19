package info.jukov.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.math.BigDecimal
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingConstants

class TickerScreen(
    private val name: String,
    private val symbol: String,
    private val onDeactivateClickListener: (symbol: String) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.Swing)
    private var blinkJob: Job? = null

    private val box = Box.createVerticalBox()
    private val deactivateButton = JButton()
    private val tickerSymbolLabel = JLabel("", SwingConstants.CENTER)
    private val tickerPriceLabel = JLabel("", SwingConstants.CENTER)

    val component: Component = box

    private var price: BigDecimal? = null

    init {
        box.setSize(200, 200)
        box.minimumSize = Dimension(200, 200)
        box.isOpaque = true

        box.add(Box.createVerticalGlue())
        box.add(tickerSymbolLabel)
        box.add(tickerPriceLabel)
        box.add(Box.createVerticalGlue())
        box.add(deactivateButton)

        tickerSymbolLabel.font = tickerSymbolLabel.font.deriveFont(0, 30f)
        tickerSymbolLabel.text = name
        tickerSymbolLabel.alignmentX = Component.CENTER_ALIGNMENT

        tickerPriceLabel.font = tickerPriceLabel.font.deriveFont(Font.BOLD, 40f)
        tickerPriceLabel.text = "-"
        tickerPriceLabel.alignmentX = Component.CENTER_ALIGNMENT

        deactivateButton.text = "Deactivate"
        deactivateButton.alignmentX = Component.CENTER_ALIGNMENT

        deactivateButton.addActionListener {
            onDeactivateClickListener(symbol)
        }
    }

    fun setPrice(price: BigDecimal?, priceFormatted: String) {
        val oldPrice = this.price
        this.price = price

        blink(oldPrice, price)

        tickerPriceLabel.text = priceFormatted
    }

    private fun blink(oldPrice: BigDecimal?, newPrice: BigDecimal?) {
        if (oldPrice == null || newPrice == null) return
        if (oldPrice == newPrice) return

        blinkJob?.cancel("New price arrived")
        blinkJob = scope.launch {
            if (oldPrice > newPrice) {
                box.background = ASK_COLOR
            } else {
                box.background = BID_COLOR
            }
            delay(500L)
            box.background = null
        }
    }

    fun onRemove() {
        scope.cancel("Ticker removed")
    }

    companion object {
        private val BID_COLOR = Color(0x74db48)
        private val ASK_COLOR = Color(0xdb4848)
    }
}