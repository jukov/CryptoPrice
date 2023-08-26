import data.DataConfig
import data.RestHelper
import data.TickerRepositoryImpl
import data.WSHelper
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.serialization.json.Json
import ui.MainScreen
import ui.TickerViewModel
import util.Logging

fun main() {
    val wsHttpClient = HttpClient {
        install(WebSockets)
    }
    val httpClient = HttpClient()
    val dataConfig = DataConfig(
        wsUrl = "wss://ws.bitmex.com/realtime",
        restUrl = "https://www.bitmex.com/api/v1"
    )
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }
    MainScreen(
        logger = Logging,
        viewModel = TickerViewModel(
            logger = Logging,
            repository = TickerRepositoryImpl(
                dataConfig = dataConfig,
                websocket = WSHelper(
                    logger = Logging,
                    httpClient = wsHttpClient
                ),
                rest = RestHelper(
                    logger = Logging,
                    httpClient = httpClient
                ),
                json = json
            )
        )
    )
}