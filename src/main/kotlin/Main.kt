import data.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.serialization.json.Json
import ui.MainScreen
import ui.TickerViewModel
import util.DecimalFormatter
import util.Logging

fun main() {
    val wsHttpClient = HttpClient {
        install(WebSockets)
    }
    val httpClient = HttpClient()
    val dataConfig = DataConfig(
        wsUrl = "wss://stream.binance.com:9443",
        restUrl = "https://api.binance.com"
    )
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }
    MainScreen(
        logger = Logging,
        viewModel = TickerViewModel(
            decimalFormatter = DecimalFormatter(),
            tickerRepository = TickerRepositoryImpl(
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
            ),
            settingsRepository = SettingsRepositoryImpl(
                logger = Logging,
                json = json
            )
        )
    )
}