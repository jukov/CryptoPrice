import data.DataConfig
import data.TickerRepositoryImpl
import data.WSHelper
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.serialization.json.Json
import ui.MainScreen
import ui.TickerViewModel
import util.Logging

fun main() {
    MainScreen(
        TickerViewModel(
            logger = Logging,
            repository = TickerRepositoryImpl(
                logger = Logging,
                dataConfig = DataConfig(
                    wsUrl = "wss://ws.bitmex.com/realtime"
                ),
                websocket = WSHelper(
                    HttpClient {
                        install(WebSockets)
                    }
                ),
                json = Json {
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                }
            )
        )
    )
}