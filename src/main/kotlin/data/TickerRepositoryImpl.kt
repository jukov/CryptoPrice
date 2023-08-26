package data

import data.model.ExchangeInfoDto
import data.model.MiniTickerDto
import domain.TickerRepository
import domain.model.Instrument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicInteger

class TickerRepositoryImpl(
    private val dataConfig: DataConfig,
    private val websocket: WSHelper,
    private val rest: RestHelper,
    private val json: Json
) : TickerRepository {

    private val wsId = AtomicInteger(1)

    private val instrumentUpdateFlow = MutableSharedFlow<Instrument>()

    private val observingSymbols = HashSet<String>()

    override suspend fun observeInstruments(): Flow<Instrument> = instrumentUpdateFlow

    override suspend fun subscribe(symbol: String) {
        observingSymbols += symbol

        val streamName = "${symbol.lowercase()}@miniTicker"

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
            val streamName = "${symbol.lowercase()}@miniTicker"

            websocket.send("{\"method\": \"UNSUBSCRIBE\",\"params\":[\"$streamName\"],\"id\": ${wsId.getAndIncrement()}}")
        }
    }

    private suspend fun handleMessage(message: String) {
        val instrument = decodeInstrument(message)?.toModel()

        instrument?.let { instrumentUpdateFlow.emit(it) }
    }

    private fun decodeInstrument(text: String): MiniTickerDto? {
        return try {
            json.decodeFromString<MiniTickerDto>(text)
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
            symbol = symbol ?: return null,
            price = null
        )
    }

    private fun MiniTickerDto.toModel(): Instrument? {
        high ?: return null
        low ?: return null
        return Instrument(
            name = symbol ?: return null,
            symbol = symbol,
            price = (high.toBigDecimal() + low.toBigDecimal()).divide("2".toBigDecimal(), RoundingMode.HALF_EVEN)
        )
    }

    companion object {
        const val ARG_SUBSCRIBE = "subscribe"
    }
}
