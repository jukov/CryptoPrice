package info.jukov.data

import info.jukov.data.model.ExchangeInfoDto
import info.jukov.data.model.RequestDto
import info.jukov.data.model.TickerDto
import info.jukov.domain.TickerRepository
import info.jukov.domain.model.Instrument
import info.jukov.domain.model.InstrumentUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.encodeToString
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

    private val errorFlow = MutableSharedFlow<Throwable>()

    private val observingSymbols = HashSet<String>()

    override suspend fun observeInstrumentUpdates(): Flow<InstrumentUpdate> = instrumentUpdateFlow

    override suspend fun observeErrors(): Flow<Throwable> = errorFlow

    override suspend fun subscribe(symbol: String) {
        subscribe(listOf(symbol))
    }

    override suspend fun subscribe(symbols: List<String>) {
        val streams = symbols
            .filterNot { observingSymbols.contains(it) }
            .map { "${it.lowercase()}@ticker" }

        if (streams.isEmpty()) return

        observingSymbols += symbols

        if (!websocket.isWebsocketStarted) {
            websocket.connect(
                "${dataConfig.wsUrl}ws/${streams.joinToString(separator = "/")}",
                messageListener = { message ->
                    handleMessage(message)
                },
                errorListener = { throwable ->
                    errorFlow.emit(throwable)
                }
            )
        } else {
            websocket.send(
                json.encodeToString(
                    RequestDto(
                        wsId.getAndIncrement(),
                        "SUBSCRIBE",
                        streams
                    )
                )
            )
        }
    }

    override suspend fun reconnect() {
        val streams = observingSymbols
            .map { "${it.lowercase()}@ticker" }

        if (streams.isEmpty()) return

        if (websocket.isWebsocketStarted) {
            error("Already connected")
        }

        websocket.connect(
            "${dataConfig.wsUrl}ws/${streams.joinToString(separator = "/")}",
            messageListener = { message ->
                handleMessage(message)
            },
            errorListener = { throwable ->
                errorFlow.emit(throwable)
            }
        )
    }

    override suspend fun unsubscribe(symbol: String) {
        observingSymbols -= symbol

        if (observingSymbols.isEmpty()) {
            websocket.disconnect()
        } else {
            val stream = "${symbol.lowercase()}@ticker"

            websocket.send(
                json.encodeToString(
                    RequestDto(
                        wsId.getAndIncrement(),
                        "UNSUBSCRIBE",
                        listOf(stream)
                    )
                )
            )
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

    override suspend fun getInstrumentList(): List<Instrument>? {
        return try {
            val response = rest.get(
                dataConfig.restUrl + "api/v3/exchangeInfo",
            ) {
                parameters.append("permissions", "SPOT")
            }

            json.decodeFromString<ExchangeInfoDto>(response).symbols
                ?.mapNotNull { it?.toModel() }
                ?: emptyList()
        } catch (e: Exception) {
            null
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

}
