package ui

import domain.SettingsRepository
import domain.TickerRepository
import domain.model.InstrumentUpdate
import domain.model.UserInstrument
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import ui.model.InstrumentPickerUiModel
import ui.model.InstrumentUiModel
import ui.model.UiEvent
import util.DecimalFormatter
import util.editIf
import java.math.RoundingMode

class TickerViewModel(
    dispatcher: CoroutineDispatcher,
    private val decimalFormatter: DecimalFormatter,
    private val tickerRepository: TickerRepository,
    private val settingsRepository: SettingsRepository,
    private val maxTickers: Int
) {

    private var scope = CoroutineScope(dispatcher)

    fun init() {
        scope.launch {
            tickerRepository.observeInstrumentUpdates().collect { newInstrument ->
                setNewPrice(newInstrument)
            }
        }

        scope.launch {
            val userInstruments = settingsRepository.getUserInstruments()
            if (userInstruments == null) {
                restoreUserInstruments(SAMPLE_INSTRUMENTS)
            } else {
                restoreUserInstruments(userInstruments)
            }
        }
    }

    private val modelFlow = MutableStateFlow<List<InstrumentUiModel>>(emptyList())
    private val eventFlow = MutableSharedFlow<UiEvent>()

    fun observeTickers(): Flow<List<InstrumentUiModel>> = modelFlow

    fun observeEvents(): Flow<UiEvent> = eventFlow

    private suspend fun restoreUserInstruments(userInstruments: List<UserInstrument>) {
        val currentModel = modelFlow.value
        require(currentModel.isEmpty()) { "No instruments should be subscribed before instruments restore" }
        modelFlow.emit(
            userInstruments.map {
                with(it) {
                    InstrumentUiModel(
                        name,
                        symbol,
                        precision,
                        null,
                        PRICE_LOADING
                    )
                }
            }
        )
        tickerRepository.subscribe(userInstruments.map { it.symbol })
    }

    private suspend fun setNewPrice(newInstrument: InstrumentUpdate) {
        val currentModel = modelFlow.value
        modelFlow.emit(
            currentModel.editIf(
                { it.symbol == newInstrument.symbol },
                {
                    val priceFormatted = decimalFormatter.formatAdjustPrecision(newInstrument.price, it.precision)
                    val postFormatPrecision = decimalFormatter.calcValuePrecision(newInstrument.price, it.precision)
                    it.copy(
                        price = newInstrument.price.setScale(postFormatPrecision, RoundingMode.HALF_EVEN),
                        priceFormatted = priceFormatted
                    )
                }
            )
        )
    }

    fun addTicker(instrument: InstrumentPickerUiModel) {
        scope.launch {
            if (addInstrument(instrument)) {
                tickerRepository.subscribe(instrument.symbol)
                saveTickers()
            }
        }
    }

    private suspend fun addInstrument(instrument: InstrumentPickerUiModel): Boolean {
        val currentModel = modelFlow.value
        if (currentModel.size >= maxTickers) {
            eventFlow.emit(UiEvent.MaxLimitReached)
            return false
        }
        if (currentModel.any { it.symbol == instrument.symbol }) {
            eventFlow.emit(UiEvent.TickerAlreadyAdded(instrument.name))
            return false
        }
        modelFlow.emit(
            currentModel.toMutableList()
                .apply {
                    add(
                        InstrumentUiModel(
                            instrument.name,
                            instrument.symbol,
                            instrument.precision,
                            null,
                            PRICE_LOADING
                        )
                    )
                }
        )
        return true
    }

    fun removeTicker(symbol: String) {
        scope.launch {
            if (removeInstrument(symbol)) {
                tickerRepository.unsubscribe(symbol)
                saveTickers()
            }
        }
    }

    private suspend fun saveTickers() {
        settingsRepository.setUserInstruments(
            modelFlow.value.map {
                UserInstrument(it.name, it.symbol, it.precision)
            }
        )
    }

    private suspend fun removeInstrument(symbol: String): Boolean {
        val currentModel = modelFlow.value
        if (currentModel.none { it.symbol == symbol }) return false
        modelFlow.emit(currentModel.filter { it.symbol != symbol })
        return true
    }

    suspend fun getAvailableInstruments(): Deferred<List<InstrumentPickerUiModel>?> =
        scope.async {
            tickerRepository.getInstrumentList()
                ?.asSequence()
                ?.map { instrument ->
                    with(instrument) {
                        InstrumentPickerUiModel(
                            name = name,
                            symbol = symbol,
                            baseAsset = baseAsset,
                            quoteAsset = quoteAsset,
                            precision = precision
                        )
                    }
                }
                ?.filter { it.quoteAsset == "USDT" }
                ?.sortedBy { it.name }
                ?.toList()
        }


    companion object {
        const val PRICE_LOADING = "Loading..."

        val SAMPLE_INSTRUMENTS = listOf(
            UserInstrument(
                "BTC/USDT",
                "BTCUSDT",
                4
            ),
            UserInstrument(
                "ETH/USDT",
                "ETHUSDT",
                4
            ),
            UserInstrument(
                "BNB/USDT",
                "BNBUSDT",
                4
            ),
        )
    }
}