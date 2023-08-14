package data

import data.model.InstrumentListDto
import domain.TickerRepository
import domain.model.Instrument
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import toBigDecimalOrNull

class TickerRepositoryImpl : TickerRepository {

    private val instrumentUpdateFlow = MutableSharedFlow<Instrument>()

    private val client = HttpClient {
        install(WebSockets)
    }
    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }

    private val observingSymbols = HashSet<String>()

    private var webSocketSessionJob: Job? = null
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val isWebsocketStarted: Boolean
        get() = webSocketSession != null

    override suspend fun observeInstrument(): Flow<Instrument> = instrumentUpdateFlow

    override suspend fun subscribe(symbol: String) {
        observingSymbols += symbol

        if (!isWebsocketStarted) {
            startAndSubscribe()
        } else {
            TODO()
//            subscribe()
        }
    }

    private suspend fun startAndSubscribe() {
        if (webSocketSessionJob != null) error("WebSocket already opened")
        if (webSocketSession != null) error("WebSocket already opened")

        val job = Job()
        webSocketSessionJob = job

        withContext(Dispatchers.IO + job) {
            launch {
                if (webSocketSession != null) error("WebSocket already opened")

                val instrumentsToObserve = observingSymbols.map { "instrument:$it" }.joinToString(separator = ",")

                try {
                    val session = client.webSocketSession("$WEBSOCKET_URL?$ARG_SUBSCRIBE=$instrumentsToObserve")
                    webSocketSession = session

                    for (message in session.incoming) {
                        withContext(Dispatchers.Default + job) {
                            handleMessage(message)
                        }
                    }
                } catch (e: Exception) {
                    System.err.println("Error while receiving: " + e.localizedMessage)
                }
            }
        }
    }

    private suspend fun handleMessage(message: Frame) {
        if (message !is Frame.Text) return
        val text = message.readText()
        println("New message: $text")
        val instrument = decodeInstrument(text)?.data?.firstOrNull()?.toModel()
        instrument?.let {
            instrumentUpdateFlow.emit(it)
        }
    }

    override suspend fun unsubscribe(symbols: String) {
        observingSymbols -= symbols

        if (observingSymbols.isEmpty()) {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
            webSocketSessionJob?.cancel("All instruments are unsubscribed")
            webSocketSessionJob = null
            webSocketSession = null
        }
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
        private const val WEBSOCKET_URL = "wss://ws.bitmex.com/realtime"
        private const val ARG_SUBSCRIBE = "subscribe"
    }
}