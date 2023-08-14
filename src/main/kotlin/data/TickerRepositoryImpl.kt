package data

import data.model.InstrumentListDto
import domain.TickerRepository
import domain.model.Instrument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import toBigDecimalOrNull

class TickerRepositoryImpl(
    private val dataConfig: DataConfig,
    private val websocket: WSHelper,
    private val json: Json
) : TickerRepository {

    private val instrumentUpdateFlow = MutableSharedFlow<Instrument>()

    private val observingSymbols = HashSet<String>()

    override suspend fun observeInstrument(): Flow<Instrument> = instrumentUpdateFlow

    override suspend fun subscribe(symbol: String) {
        observingSymbols += symbol

        if (!websocket.isWebsocketStarted) {
            connectAndSubscribe()
        } else {
            TODO()
//            subscribe()
        }
    }

    private suspend fun connectAndSubscribe() {
        val instrumentsToObserve = observingSymbols.map { "instrument:$it" }.joinToString(separator = ",")

        websocket.connect("${dataConfig.wsUrl}?$ARG_SUBSCRIBE=$instrumentsToObserve") { message ->
            handleMessage(message)
        }
    }

    override suspend fun unsubscribe(symbols: String) {
        observingSymbols -= symbols

        if (observingSymbols.isEmpty()) {
            websocket.disconnect()
        }
    }

    private suspend fun handleMessage(message: String) {
        val instrument = decodeInstrument(message)?.data?.firstOrNull()?.toModel()

        instrument?.let { instrumentUpdateFlow.emit(it) }
    }

    private fun decodeInstrument(text: String): InstrumentListDto? {
        return try {
            json.decodeFromString<InstrumentListDto>(text)
        } catch (e: Throwable) {
//            println(e)
            null
        }
    }

    private fun InstrumentListDto.InstrumentDto.toModel(): Instrument? {
        return Instrument(
            symbol ?: return null,
            fairPrice.toBigDecimalOrNull() ?: return null
        )
    }

    companion object {
        const val ARG_SUBSCRIBE = "subscribe"
    }
}