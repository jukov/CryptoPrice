package ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.slf4j.Logger
import ui.model.InstrumentUiModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridLayout
import javax.swing.*
import kotlin.system.exitProcess

class MainScreen(
    private val logger: Logger,
    private val viewModel: TickerViewModel
) {
    private val scope = CoroutineScope(Dispatchers.Swing)

    private val frame = JFrame()
    private val tickerPanel = JPanel()
    private val hintLabel = JLabel(
        "<html><div style='text-align: center;'>No tickers. Type ticker name and press \"Add Ticker\"</html>",
        SwingConstants.CENTER
    )
    private val tickerLayout = GridLayout(1, 1)

    private val tickerScreens = HashMap<String, TickerScreen>()
    private val newTickerScreen = NewTickerScreen(viewModel::addTicker)

    init {
        viewModel.init()

        initUi()

        getAvailableInstruments()

        scope.launch {
            viewModel.observeTickers().collect { model ->
                setTickers(model)
            }
        }
    }

    private fun getAvailableInstruments() {
        scope.launch {
            viewModel.getAvailableInstruments().await().let { instruments ->
                if (!instruments.isNullOrEmpty()) {
                    newTickerScreen.setAvailableInstruments(instruments)
                } else {
                    handleAvailableInstrumentsError()
                }
            }
        }
    }

    private fun handleAvailableInstrumentsError() {
        val result = JOptionPane.showOptionDialog(
            frame,
            "An error occurred while fetching instruments. Reload?",
            "Error",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            arrayOf("Yes, reload", "No, exit"),
            "Yes, reload"
        )
        if (result == 0) {
            getAvailableInstruments()
        } else {
            exitProcess(0)
        }
    }

    private fun setTickers(model: List<InstrumentUiModel>) {
        if (model.isEmpty()) {
            frame.contentPane.remove(tickerPanel)
            frame.contentPane.add(hintLabel, BorderLayout.CENTER)
        } else {
            frame.contentPane.remove(hintLabel)
            frame.contentPane.add(tickerPanel, BorderLayout.CENTER)

            model.forEach { instrument ->
                if (tickerScreens.containsKey(instrument.symbol)) {
                    tickerScreens[instrument.symbol]?.setPrice(instrument.price, instrument.priceFormatted)
                } else {
                    addTicker(instrument)
                }
            }

            tickerScreens.keys.minus(model.map { it.symbol }.toSet()).forEach {  removedSymbol ->
                removeTicker(removedSymbol)
            }
        }
    }

    private fun initUi() {
        newTickerScreen.initUi()

        val pane = frame.contentPane
        pane.add(hintLabel, BorderLayout.CENTER)
        pane.add(newTickerScreen.component, BorderLayout.PAGE_START)

        tickerPanel.layout = tickerLayout

        hintLabel.alignmentX = Component.CENTER_ALIGNMENT

        frame.setSize(TICKER_SIZE, TICKER_SIZE + NEW_TICKER_SIZE)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isResizable = false
        frame.setLocationRelativeTo(null)
        frame.title = "Crypto Price"
        frame.isVisible = true
    }

    private fun addTicker(instrument: InstrumentUiModel) {
        if (tickerScreens.size > MAX_TICKERS) return
        if (tickerScreens.containsKey(instrument.symbol)) return

        val ticker = TickerScreen(instrument.name, instrument.symbol, viewModel::removeTicker)
        ticker.setPrice(instrument.price, instrument.priceFormatted)
        tickerScreens[instrument.symbol] = ticker

        tickerPanel.add(ticker.component)

        adjustGrid()

        tickerPanel.revalidate()
    }

    private fun adjustGrid() {
        if (tickerScreens.isEmpty()) {
            frame.setSize(TICKER_SIZE, TICKER_SIZE + 50)

            logger.info("Grid adjusted to hint (0 tickers)")
        } else {
            val column = tickerScreens.size.coerceAtMost(MAX_COLUMNS)
            val row = 1 + (tickerScreens.size - 1) / MAX_COLUMNS

            tickerLayout.columns = column
            tickerLayout.rows = row

            frame.setSize(column * TICKER_SIZE, NEW_TICKER_SIZE + row * TICKER_SIZE)
            logger.info("Grid adjusted to $row rows and $column columns")
        }
    }

    private fun removeTicker(symbol: String) {
        val ticker = tickerScreens.remove(symbol) ?: return
        ticker.onRemove()
        tickerPanel.remove(ticker.component)
        adjustGrid()
        tickerPanel.revalidate()
    }

    companion object {
        private const val MAX_ROWS = 3
        private const val MAX_COLUMNS = 4
        private const val MAX_TICKERS = MAX_ROWS * MAX_COLUMNS
        private const val TICKER_SIZE = 220
        private const val NEW_TICKER_SIZE = 60
    }
}