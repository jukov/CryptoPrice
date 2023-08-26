package ui

import domain.TickerRepository
import domain.model.Instrument
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.Logger
import ui.model.InstrumentPickerItem
import ui.model.ObservingInstrumentItem
import ui.model.ObservingInstrumentsModel
import util.editIf

class TickerViewModel(
    private val logger: Logger,
    private val repository: TickerRepository
) {

    private var scope = CoroutineScope(Dispatchers.IO)

    init {
        //todo is it not working after screen close?
        scope.launch {
            repository.observeInstruments().collect { newInstrument ->
                setNewPrice(newInstrument)
            }
        }
    }

    private val modelFlow = MutableStateFlow(
        ObservingInstrumentsModel(tickers = emptyList())
    )

    fun observeModel(): Flow<ObservingInstrumentsModel> = modelFlow

    private suspend fun setNewPrice(newInstrument: Instrument) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.editIf(
                    { it.symbol == newInstrument.symbol },
                    { it.copy(price = newInstrument.price?.toPlainString() ?: PRICE_EMPTY) }
                )
            )
        )
    }

    fun subscribe(symbol: String) {
        logger.error("Subscribe start")
        scope.launch {
            logger.error("Subscribe start in coroutine")
            addInstrument(symbol)
            logger.error("Instrument added")
            repository.subscribe(symbol)
            logger.error("Subscribed")
        }
    }

    private suspend fun addInstrument(symbol: String) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.copy(
                tickers = currentModel.tickers.toMutableList()
                    .apply { add(ObservingInstrumentItem(symbol, PRICE_EMPTY)) }
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
                .map {
                    InstrumentPickerItem(
                        it.name,
                        it.symbol
                    )
                }
                .sortedBy { it.name }
        }


    companion object {
        private const val PRICE_EMPTY = "-"
    }
}