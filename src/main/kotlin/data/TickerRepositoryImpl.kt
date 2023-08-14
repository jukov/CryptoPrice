package data

import data.model.InstrumentListDto
import domain.TickerRepository
import domain.model.Instrument
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import toBigDecimalOrNull

class TickerRepositoryImpl : TickerRepository {

    private val client = HttpClient {
        install(WebSockets)
    }
    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }

    override fun observeIncrement(): Flow<Int> = flow {
        for (i in 0..1_000_000) {
            emit(i)
            delay(1000L)
        }
    }

    override suspend fun observeInstrument(): Flow<Instrument> = flow {
        client.webSocket("wss://ws.bitmex.com/realtime?subscribe=instrument:XBTUSD") {
            try {
                for (message in incoming) {
                    message as? Frame.Text ?: continue
                    val text = message.readText()
                    println("New message: $text")
                    val instrument = decodeInstrument(text)?.data?.firstOrNull()?.toModel()
                    instrument?.let {
                        emit(it)
                    }
                }
            } catch (e: Exception) {
                println("Error while receiving: " + e.localizedMessage)
            }
        }
    }

    private fun decodeInstrument(text: String): InstrumentListDto? {
        return try {
            json.decodeFromString<InstrumentListDto>(text)
        } catch (e: Throwable) {
            println(e)
            null
        }
    }

    private fun InstrumentListDto.InstrumentDto.toModel(): Instrument? {
        return Instrument(
            symbol ?: return null,
            fairPrice.toBigDecimalOrNull() ?: return null
        )
    }
}