import data.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import ui.MainScreen
import ui.TickerViewModel
import util.DecimalFormatterImpl
import util.Logging
import java.time.Duration

fun main() {
    val wsHttpClient = HttpClient {
        install(WebSockets) {
            pingInterval = Duration.ofSeconds(15).toMillis()
        }
    }
    val httpClient = HttpClient()
    val dataConfig = DataConfig(
        wsUrl = "wss://stream.binance.com:9443/",
        restUrl = "https://api.binance.com/"
    )
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }
    MainScreen(
        logger = Logging,
        viewModel = TickerViewModel(
            dispatcher = Dispatchers.IO,
            decimalFormatter = DecimalFormatterImpl(),
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
            ),
            Constants.MAX_TICKERS
        )
    )
}