package data

import data.model.InstrumentListDtoItem
import data.model.InstrumentPriceListDto
import domain.TickerRepository
import domain.model.Instrument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import util.toBigDecimalOrNull

class TickerRepositoryImpl(
    private val dataConfig: DataConfig,
    private val websocket: WSHelper,
    private val rest: RestHelper,
    private val json: Json
) : TickerRepository {

    private val instrumentUpdateFlow = MutableSharedFlow<Instrument>()

    private val observingSymbols = HashSet<String>()

    override suspend fun observeInstruments(): Flow<Instrument> = instrumentUpdateFlow

    override suspend fun subscribe(symbol: String) {
        observingSymbols += symbol

        if (!websocket.isWebsocketStarted) {
            val instrumentsToObserve = observingSymbols.map { "instrument:$it" }.joinToString(separator = ",")

            websocket.connect("${dataConfig.wsUrl}?$ARG_SUBSCRIBE=$instrumentsToObserve") { message ->
                handleMessage(message)
            }
        } else {
            websocket.send("{\"op\": \"subscribe\", \"args\": [\"instrument:$symbol\"]}")
        }
    }

    override suspend fun unsubscribe(symbol: String) {
        observingSymbols -= symbol

        if (observingSymbols.isEmpty()) {
            websocket.disconnect()
        } else {
            websocket.send("{\"op\": \"unsubscribe\", \"args\": [\"instrument:$symbol\"]}")
        }
    }

    private suspend fun handleMessage(message: String) {
        val instrument = decodeInstrument(message)?.data?.firstOrNull()?.toModel()

        instrument?.let { instrumentUpdateFlow.emit(it) }
    }

    private fun decodeInstrument(text: String): InstrumentPriceListDto? {
        return try {
            json.decodeFromString<InstrumentPriceListDto>(text)
        } catch (e: Throwable) {
            null
        }
    }

    private fun InstrumentPriceListDto.InstrumentDto.toModel(): Instrument? {
        return Instrument(
            "",//TODO provide name
            symbol ?: return null,
            fairPrice.toBigDecimalOrNull() ?: return null
        )
    }

    private fun InstrumentListDtoItem.toModel(): Instrument? {
        return Instrument(
            "$underlying/$quoteCurrency",
            symbol?.filter { it != '_' } ?: return null,
            null
        )
    }

    override suspend fun getInstrumentList(): List<Instrument> =
        rest.getInstrumentList().mapNotNull { it.toModel() }

    companion object {
        const val ARG_SUBSCRIBE = "subscribe"
    }
}