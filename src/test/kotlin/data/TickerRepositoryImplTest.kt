package data

import info.jukov.data.DataConfig
import info.jukov.data.RestHelper
import info.jukov.data.TickerRepositoryImpl
import info.jukov.data.WSHelper
import info.jukov.domain.model.Instrument
import info.jukov.domain.model.InstrumentUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TickerRepositoryImplTest {

    private val dataConfig = DataConfig(
        wsUrl = testWsUrl,
        restUrl = testRestUrl
    )
    private val wsHelper: WSHelper = mockk<WSHelper>()
    private val restHelper: RestHelper = mockk<RestHelper>()
    private val repository = TickerRepositoryImpl(
        dataConfig,
        wsHelper,
        restHelper,
        Json {
            coerceInputValues = true
            ignoreUnknownKeys = true
        }
    )

    @Test
    fun `subscribe to ws`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any(), any()) } returns Unit

        repository.subscribe(testInstrument)

        coVerify(exactly = 1) { wsHelper.connect("${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker", any(), any()) }
    }

    @Test
    fun `subscribe to ws multiple`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any(), any()) } returns Unit

        repository.subscribe(listOf(testInstrument, testInstrument2))

        coVerify(exactly = 1) {
            wsHelper.connect(
                "${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker/${testInstrument2.lowercase()}@ticker",
                any(),
                any()
            )
        }
    }

    @Test
    fun `subscribe twice`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any(), any()) } returns Unit
        coEvery { wsHelper.send(any()) } returns Unit

        repository.subscribe(testInstrument)

        every { wsHelper.isWebsocketStarted } returns true

        repository.subscribe(testInstrument2)

        coVerify(exactly = 1) { wsHelper.connect("${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker", any(), any()) }
        coVerify(exactly = 1) { wsHelper.send("{\"id\":1,\"method\":\"SUBSCRIBE\",\"params\":[\"${testInstrument2.lowercase()}@ticker\"]}") }
    }

    @Test
    fun `subscribe same ticker twice`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any(), any()) } returns Unit
        coEvery { wsHelper.send(any()) } returns Unit

        repository.subscribe(testInstrument)

        every { wsHelper.isWebsocketStarted } returns true

        repository.subscribe(testInstrument)

        coVerify(exactly = 1) { wsHelper.connect("${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker", any(), any()) }
    }

    @Test
    fun `subscribe twice and unsubscribe`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any(), any()) } returns Unit
        coEvery { wsHelper.send(any()) } returns Unit
        coEvery { wsHelper.disconnect() } returns Unit

        repository.subscribe(testInstrument)

        every { wsHelper.isWebsocketStarted } returns true

        repository.subscribe(testInstrument2)

        repository.unsubscribe(testInstrument)
        repository.unsubscribe(testInstrument2)

        coVerify(exactly = 1) { wsHelper.connect("${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker", any(), any()) }
        coVerify(exactly = 1) { wsHelper.send("{\"id\":1,\"method\":\"SUBSCRIBE\",\"params\":[\"${testInstrument2.lowercase()}@ticker\"]}") }
        coVerify(exactly = 1) { wsHelper.send("{\"id\":2,\"method\":\"UNSUBSCRIBE\",\"params\":[\"${testInstrument.lowercase()}@ticker\"]}") }
        coVerify(exactly = 1) { wsHelper.disconnect() }
    }

    @Test
    fun `unsubscribe from ws`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any(), any()) } returns Unit
        coEvery { wsHelper.disconnect() } returns Unit

        repository.subscribe(testInstrument)
        repository.unsubscribe(testInstrument)

        coVerify(exactly = 1) { wsHelper.connect("${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker", any(), any()) }
        coVerify(exactly = 1) { wsHelper.disconnect() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `subscribe and receive update`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), captureLambda(), any()) } coAnswers {
            lambda<suspend (String) -> Unit>().captured.invoke(testTickerResponse)
        }

        val updates = ArrayList<InstrumentUpdate>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeInstrumentUpdates().collect {
                updates += it
            }
        }

        repository.subscribe(testInstrument)

        testScheduler.advanceTimeBy(10L)

        assertEquals(1, updates.size)
        assertEquals(testUpdate, updates.first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `reconnect after failure`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), captureLambda(), captureLambda()) } coAnswers {
            secondArg<suspend (String) -> Unit>().invoke(testTickerResponse)
            thirdArg<suspend (Throwable) -> Unit>().invoke(IllegalStateException())
        }

        val updates = ArrayList<InstrumentUpdate>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeInstrumentUpdates().collect {
                updates += it
            }
        }

        repository.subscribe(testInstrument)

        testScheduler.advanceTimeBy(10L)

        repository.reconnect()

        testScheduler.advanceTimeBy(10L)

        coVerify(exactly = 2) { wsHelper.connect("${dataConfig.wsUrl}ws/${testInstrument.lowercase()}@ticker", any(), any()) }
        assertEquals(2, updates.size)
        assertEquals(testUpdate, updates.first())
    }

    @Test
    fun `get instrument list`() = runTest {
        coEvery { restHelper.get(any(), any()) } returns testExchangeInfoResponse

        val result = repository.getInstrumentList()

        assertEquals(listOf(testInstrumentModel), result)
    }


    companion object {
        val testWsUrl = "https://wss.aboba.com/"
        val testRestUrl = "https://rest.aboba.com/"
        val testInstrument = "DOGEUSDT"
        val testInstrument2 = "PEPEUSDT"

        val testInstrumentModel = Instrument(
            "BNB/BTC",
            "BNB",
            "BTC",
            "BNBBTC",
            8
        )
        val testExchangeInfoResponse = """
                {
                    "timezone": "UTC",
                    "serverTime": 123,
                    "symbols": [
                        {
                            "symbol": "BNBBTC",
                            "status": "TRADING",
                            "baseAsset": "BNB",
                            "baseAssetPrecision": 8,
                            "quoteAsset": "BTC",
                            "quoteAssetPrecision": 8
                        }
                    ]
                }
            """.trimIndent()

        val testUpdate = InstrumentUpdate(
            "BNBBTC",
            "0.0025".toBigDecimal()
        )
        val testTickerResponse = """{ "s": "BNBBTC", "c": "0.0025"}"""
    }
}