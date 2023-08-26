package ui

import domain.TickerRepository
import domain.model.InstrumentUpdate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ui.model.InstrumentPickerItem
import ui.model.ObservingInstrumentItem
import ui.model.ObservingInstrumentsModel
import util.DecimalFormatter
import util.editIf

class TickerViewModel(
    private val decimalFormatter: DecimalFormatter,
    private val repository: TickerRepository
) {

    private var scope = CoroutineScope(Dispatchers.IO)

    init {
        //todo is it not working after screen close?
        scope.launch {
            repository.observeInstrumentUpdates().collect { newInstrument ->
                setNewPrice(newInstrument)
            }
        }
    }

    private val modelFlow = MutableStateFlow(
        ObservingInstrumentsModel(tickers = emptyList())
    )

    fun observeModel(): Flow<ObservingInstrumentsModel> = modelFlow

    private suspend fun setNewPrice(newInstrument: InstrumentUpdate) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.editIf(
                    { it.symbol == newInstrument.symbol },
                    { it.copy(price = decimalFormatter.formatAdjustPrecision(newInstrument.price, it.precision)) }
                )
            )
        )
    }

    fun subscribe(instrument: InstrumentPickerItem) {
        scope.launch {
            addInstrument(instrument)
            repository.subscribe(instrument.symbol)
        }
    }

    private suspend fun addInstrument(instrument: InstrumentPickerItem) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.toMutableList()
                    .apply {
                        add(
                            ObservingInstrumentItem(
                                instrument.name,
                                instrument.symbol,
                                instrument.precision,
                                PRICE_LOADING
                            )
                        )
                    }
            )
        )
    }

    fun unsubscribe(symbol: String) {
        scope.launch {
            removeInstrument(symbol)
            repository.unsubscribe(symbol)
        }
    }

    private suspend fun removeInstrument(symbol: String) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.filter { it.symbol != symbol }
            )
        )
    }

    suspend fun getAvailableSymbols(): Deferred<List<InstrumentPickerItem>> =
        scope.async {
            repository.getInstrumentList()
                .asSequence()
                .map { instrument ->
                    with(instrument) {
                        InstrumentPickerItem(
                            name = name,
                            baseCurrency = baseAsset,
                            quoteCurrency = quoteAsset,
                            symbol = symbol,
                            precision = precision
                        )
                    }
                }
                .filter { it.quoteCurrency == "USDT" }
                .sortedBy { it.name }
                .toList()
        }


    companion object {
        private const val PRICE_LOADING = "Loading..."
    }
}