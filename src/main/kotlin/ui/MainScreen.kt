package ui

import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class MainScreen(
    private val viewModel: TickerViewModel
) {
    private val frame = JFrame()
    private val newTickerBox = Box.createHorizontalBox()
    private val newTickerTextFieldBox = Box.createVerticalBox()
    private val tickerPanel = JPanel()
    private val newTickerButton = JButton()
    private val newTickerLabel = JLabel()
    private val newTickerTextField = JTextField()
    private val tickerLayout = GridLayout(1, 1)

    private val tickers = HashMap<String, TickerScreen>()

    private val onTickerDisconnect = { symbol: String ->
        removeTicker(symbol)
    }

    init {
        initUi()

        runBlocking {
            viewModel.observePrice().collect { instrument ->
                tickers[instrument.symbol]?.setPrice(instrument.price)
            }
        }
    }
    //TODO add ticker immediatly
    //TODO long deactivate

    private fun initUi() {
        val pane = frame.contentPane
        pane.add(newTickerBox, BorderLayout.PAGE_START)
        pane.add(tickerPanel, BorderLayout.CENTER)

        tickerPanel.layout = tickerLayout

        newTickerBox.add(newTickerButton)
        newTickerBox.add(newTickerTextFieldBox)
        newTickerTextFieldBox.add(newTickerLabel)
        newTickerTextFieldBox.add(newTickerTextField)

        newTickerLabel.text = "Ticker name"

        newTickerTextField.text = "XBTUSD"

        newTickerButton.maximumSize = Dimension(newTickerButton.width, Int.MAX_VALUE)
        newTickerButton.text = "Add Ticker"
        newTickerButton.alignmentY = Component.CENTER_ALIGNMENT

        newTickerButton.addActionListener {
            newTickerTextField.text?.let(::addTicker)
        }

        frame.setSize(200, 250)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.title = "Crypto Price"
        frame.isVisible = true
//        frame.isResizable = false
    }

    private fun addTicker(symbol: String) {
        if (tickers.size > MAX_TICKERS) return
        if (tickers.containsKey(symbol)) return

        val ticker = TickerScreen(symbol, onTickerDisconnect)
        tickers[symbol] = ticker

        tickerPanel.add(ticker.component)

        adjustGrid()

        viewModel.subscribe(symbol)
    }

    private fun adjustGrid() {
        val row = 1 + (tickers.size - 1) / MAX_ROWS
        val column = tickers.size.coerceAtMost(MAX_COLUMNS)

        tickerLayout.columns = column
        tickerLayout.rows = row

        frame.setSize(column * 200, newTickerBox.height + row * 200)
    }

    private fun removeTicker(symbol: String) {
        val ticker = tickers.remove(symbol) ?: return
        tickerPanel.remove(ticker.component)
        adjustGrid()
        viewModel.unsubscribe(symbol)
    }

    companion object {
        const val MAX_ROWS = 4
        const val MAX_COLUMNS = 4
        const val MAX_TICKERS = MAX_ROWS * MAX_COLUMNS
    }
}