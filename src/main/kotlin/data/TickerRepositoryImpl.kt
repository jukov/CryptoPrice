package data

import data.model.ExchangeInfoDto
import data.model.TickerDto
import domain.TickerRepository
import domain.model.Instrument
import domain.model.InstrumentUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

class TickerRepositoryImpl(
    private val dataConfig: DataConfig,
    private val websocket: WSHelper,
    private val rest: RestHelper,
    private val json: Json
) : TickerRepository {

    private val wsId = AtomicInteger(1)

    private val instrumentUpdateFlow = MutableSharedFlow<InstrumentUpdate>()

    private val observingSymbols = HashSet<String>()

    override suspend fun observeInstrumentUpdates(): Flow<InstrumentUpdate> = instrumentUpdateFlow

    override suspend fun subscribe(symbol: String) {
        observingSymbols += symbol

        val streamName = "${symbol.lowercase()}@ticker"

        if (!websocket.isWebsocketStarted) {
            websocket.connect("${dataConfig.wsUrl}/ws/$streamName") { message ->
                handleMessage(message)
            }
        } else {
            websocket.send(
                "{\"method\": \"SUBSCRIBE\",\"params\":[\"$streamName\"],\"id\": ${wsId.getAndIncrement()}}"
            )
        }
    }

    override suspend fun unsubscribe(symbol: String) {
        observingSymbols -= symbol

        if (observingSymbols.isEmpty()) {
            websocket.disconnect()
        } else {
            val streamName = "${symbol.lowercase()}@ticker"

            websocket.send("{\"method\": \"UNSUBSCRIBE\",\"params\":[\"$streamName\"],\"id\": ${wsId.getAndIncrement()}}")
        }
    }

    private suspend fun handleMessage(message: String) {
        val instrument = decodeInstrument(message)?.toModel()

        instrument?.let { instrumentUpdateFlow.emit(it) }
    }

    private fun decodeInstrument(text: String): TickerDto? {
        return try {
            json.decodeFromString<TickerDto>(text)
        } catch (e: Throwable) {
            null
        }
    }

    override suspend fun getInstrumentList(): List<Instrument> {
        return try {
            val response = rest.get(
                dataConfig.restUrl + "/api/v3/exchangeInfo",
            ) {
                parameters.append("permissions", "SPOT")
            }

            json.decodeFromString<ExchangeInfoDto>(response).symbols
                ?.mapNotNull { it?.toModel() }
                ?: emptyList() //TODO error
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ExchangeInfoDto.Symbol.toModel(): Instrument? {
        baseAsset ?: return null
        quoteAsset ?: return null
        return Instrument(
            name = "$baseAsset/$quoteAsset",
            baseAsset = baseAsset,
            quoteAsset = quoteAsset,
            symbol = symbol ?: return null,
            precision = baseAssetPrecision ?: return null
        )
    }

    private fun TickerDto.toModel(): InstrumentUpdate? {
        return InstrumentUpdate(
            symbol = symbol ?: return null,
            price = lastPrice?.toBigDecimal() ?: return null
        )
    }

    companion object {
        const val ARG_SUBSCRIBE = "subscribe"
    }
}
