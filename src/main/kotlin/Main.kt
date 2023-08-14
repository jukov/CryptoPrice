import data.DataConfig
import data.TickerRepositoryImpl
import data.WSHelper
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.serialization.json.Json
import ui.MainScreen
import ui.TickerViewModel

fun main(args: Array<String>) {
    MainScreen(
        TickerViewModel(
            TickerRepositoryImpl(
                DataConfig(
                    wsUrl = "wss://ws.bitmex.com/realtime"
                ),
                WSHelper(
                    HttpClient() {
                        install(WebSockets)
                    }
                ),
                Json {
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                }
            )
        )
    )
}