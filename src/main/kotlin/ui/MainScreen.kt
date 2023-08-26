package ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.slf4j.Logger
import ui.model.InstrumentPickerItem
import ui.model.ObservingInstrumentsModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class MainScreen(
    private val logger: Logger,
    private val viewModel: TickerViewModel
) {
    //TODO start in center of screen
    //TODO ticker styling
    //TODO header styling
    private val scope = CoroutineScope(Dispatchers.Swing)

    private val frame = JFrame()
    private val newTickerBox = Box.createHorizontalBox()
    private val newTickerTextFieldBox = Box.createVerticalBox()
    private val tickerPanel = JPanel()
    private val newTickerButton = JButton("Add Ticker")
    private val newTickerLabel = JLabel("Ticker name")
    private val newTickerComboBox = JComboBox<InstrumentPickerItem>()
    private val hintLabel = JLabel(
        "<html><div style='text-align: center;'>No tickers. Type ticker name and press \"Add Ticker\"</html>",
        SwingConstants.CENTER
    )
    private val tickerLayout = GridLayout(1, 1)

    private val tickers = HashMap<String, TickerScreen>()

    private val onTickerDisconnect = { symbol: String ->
        removeTicker(symbol)
    }

    init {
        initUi()

        scope.launch {
            viewModel.getAvailableSymbols().await().let { instruments ->
                instruments.forEach {
                    newTickerComboBox.addItem(it)
                }
                if (instruments.isNotEmpty()) {
                    newTickerComboBox.selectedItem = instruments.first()
                }
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
        pane.add(newTickerBox, BorderLayout.PAGE_START)
        pane.add(hintLabel, BorderLayout.CENTER)

        tickerPanel.layout = tickerLayout

        newTickerBox.add(newTickerButton)
        newTickerBox.add(newTickerTextFieldBox)
        newTickerTextFieldBox.add(newTickerLabel)
        newTickerTextFieldBox.add(newTickerComboBox)

        hintLabel.alignmentX = Component.CENTER_ALIGNMENT

        newTickerComboBox.isEditable = true
//        newTickerComboBox.addItemListener { e ->
//            if (e.stateChange == ItemEvent.SELECTED) {
//
//            }
//        }

        newTickerButton.maximumSize = Dimension(newTickerButton.width, Int.MAX_VALUE)
        newTickerButton.alignmentY = Component.CENTER_ALIGNMENT
        newTickerButton.alignmentX = Component.CENTER_ALIGNMENT

        newTickerButton.addActionListener {
            when (val item = newTickerComboBox.selectedItem) {
                is InstrumentPickerItem -> addTicker(item.symbol)
                is String -> addTicker(item)
            }
        }

        frame.setSize(TICKER_SIZE, 250)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.title = "Crypto Price"
        frame.isVisible = true
    }

    private fun addTicker(symbol: String) {
        if (tickers.size > MAX_TICKERS) return
        if (tickers.containsKey(symbol)) return

        val ticker = TickerScreen(symbol, onTickerDisconnect)
        tickers[symbol] = ticker

        tickerPanel.add(ticker.component)

        adjustGrid()

        tickerPanel.revalidate()

        viewModel.subscribe(symbol)
    }

    private fun adjustGrid() {
        val column = tickers.size.coerceAtMost(MAX_COLUMNS)
        val row = 1 + (tickers.size - 1) / MAX_COLUMNS

        tickerLayout.columns = column
        tickerLayout.rows = row

        frame.setSize(column * TICKER_SIZE, newTickerBox.height + row * TICKER_SIZE)
        logger.info("Grid adjusted to $row rows and $column columns")
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
        const val TICKER_SIZE = 200
    }
}