package ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.slf4j.Logger
import ui.model.InstrumentPickerItem
import ui.model.ObservingInstrumentsModel
import java.awt.*
import javax.swing.*

class MainScreen(
    private val logger: Logger,
    private val viewModel: TickerViewModel
) {
    //TODO ticker styling
    private val scope = CoroutineScope(Dispatchers.Swing)

    private val frame = JFrame()
    private val newTickerPanel = JPanel()
    private val tickerPanel = JPanel()
    private val newTickerButton = JButton("Add Ticker")
    private val newTickerComboBox = JComboBox<InstrumentPickerItem>()
    private val hintLabel = JLabel(
        "<html><div style='text-align: center;'>No tickers. Type ticker name and press \"Add Ticker\"</html>",
        SwingConstants.CENTER
    )
    private val newTickerLayout = GridBagLayout()
    private val tickerLayout = GridLayout(1, 1)

    private val tickers = HashMap<String, TickerScreen>()

    private val onTickerDisconnect = { symbol: String ->
        removeTicker(symbol)
    }

    init {
        initUi()

        scope.launch {
            viewModel.getAvailableSymbols().await().let { instruments ->
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
        }

        scope.launch {
            viewModel.observeModel().collect { model ->
                setModel(model)
            }
        }
    }

    private fun setModel(model: ObservingInstrumentsModel) {
        if (model.tickers.isEmpty()) {
            frame.contentPane.remove(tickerPanel)
            frame.contentPane.add(hintLabel, BorderLayout.CENTER)
        } else {
            frame.contentPane.remove(hintLabel)
            frame.contentPane.add(tickerPanel, BorderLayout.CENTER)

            model.tickers.forEach { instrument ->
                tickers[instrument.symbol]?.setPrice(instrument.price)
            }
        }
    }

    private fun initUi() {
        val pane = frame.contentPane
        pane.add(newTickerPanel, BorderLayout.PAGE_START)
        pane.add(hintLabel, BorderLayout.CENTER)

        tickerPanel.layout = tickerLayout
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

        hintLabel.alignmentX = Component.CENTER_ALIGNMENT

        newTickerButton.maximumSize = Dimension(newTickerButton.width, Int.MAX_VALUE)
        newTickerButton.alignmentY = Component.CENTER_ALIGNMENT
        newTickerButton.alignmentX = Component.CENTER_ALIGNMENT
        newTickerButton.isEnabled = false

        newTickerButton.addActionListener {
            (newTickerComboBox.selectedItem as? InstrumentPickerItem)
                ?.let(::addTicker)
        }

        frame.setSize(TICKER_SIZE, TICKER_SIZE + 50)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.setLocationRelativeTo(null)
        frame.title = "Crypto Price"
        frame.isVisible = true
    }

    private fun addTicker(instrument: InstrumentPickerItem) {
        if (tickers.size > MAX_TICKERS) return
        if (tickers.containsKey(instrument.symbol)) return

        val ticker = TickerScreen(instrument, onTickerDisconnect)
        tickers[instrument.symbol] = ticker

        tickerPanel.add(ticker.component)

        adjustGrid()

        tickerPanel.revalidate()

        viewModel.subscribe(instrument)
    }

    private fun adjustGrid() {
        if (tickers.isEmpty()) {
            frame.setSize(TICKER_SIZE, TICKER_SIZE + 50)

            logger.info("Grid adjusted to hint (0 tickers)")
        } else {
            val column = tickers.size.coerceAtMost(MAX_COLUMNS)
            val row = 1 + (tickers.size - 1) / MAX_COLUMNS

            tickerLayout.columns = column
            tickerLayout.rows = row

            frame.setSize(column * TICKER_SIZE, newTickerPanel.height + row * TICKER_SIZE)
            logger.info("Grid adjusted to $row rows and $column columns")
        }
    }

    private fun removeTicker(symbol: String) {
        val ticker = tickers.remove(symbol) ?: return
        tickerPanel.remove(ticker.component)
        adjustGrid()
        tickerPanel.revalidate()
        viewModel.unsubscribe(symbol)
    }

    companion object {
        const val MAX_ROWS = 3
        const val MAX_COLUMNS = 5
        const val MAX_TICKERS = MAX_ROWS * MAX_COLUMNS
        const val TICKER_SIZE = 220
        private val LOADING_ITEM = InstrumentPickerItem(
            "Loading instruments...", "",
            "",
            "",
            0
        )
    }
}