package ui

import domain.TickerRepository
import domain.model.Instrument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.slf4j.Logger
import ui.model.InstrumentUiModel
import ui.model.TickersUiModel
import util.editIf

class TickerViewModel(
    private val logger: Logger,
    private val repository: TickerRepository
) {

    private var scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            repository.observeInstrument().collect { newInstrument ->
                setNewPrice(newInstrument)
            }
        }
    }

    private val modelFlow = MutableStateFlow(
        TickersUiModel(tickers = emptyList())
    )

    fun observeModel(): Flow<TickersUiModel> = modelFlow

    private suspend fun setNewPrice(newInstrument: Instrument) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.editIf(
                    { it.symbol == newInstrument.symbol },
                    { it.copy(price = newInstrument.price.toPlainString()) }
                )
            )
        )
    }

    fun subscribe(symbol: String) {
        scope.launch {
            addInstrument(symbol)
            repository.subscribe(symbol)
        }
    }

    private suspend fun addInstrument(symbol: String) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.toMutableList()
                    .apply { add(InstrumentUiModel(symbol, PRICE_EMPTY)) }
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
                tickers = currentModel.tickers.filter { it.symbol == symbol }
            )
        )
    }

    companion object {
        private const val PRICE_EMPTY = "-"
    }
}