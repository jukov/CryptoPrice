package data

import data.TickerRepositoryImpl.Companion.ARG_SUBSCRIBE
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class TickerRepositoryImplTest {

    private val dataConfig = DataConfig(wsUrl = testUrl)
    private val wsHelper: WSHelper = mockk<WSHelper>()
    private val repository = TickerRepositoryImpl(
        dataConfig,
        wsHelper,
        Json {
            coerceInputValues = true
            ignoreUnknownKeys = true
        }
    )

    @Test
    fun `subscribed to ws`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any()) } returns Unit

        repository.subscribe(testInstrument)

        coVerify(exactly = 1) { wsHelper.connect("$testUrl?$ARG_SUBSCRIBE=instrument:$testInstrument", any()) }
    }

    @Test
    fun `unsubscribed from ws`() = runTest {
        every { wsHelper.isWebsocketStarted } returns false
        coEvery { wsHelper.connect(any(), any()) } returns Unit
        coEvery { wsHelper.disconnect() } returns Unit

        repository.subscribe(testInstrument)
        repository.unsubscribe(testInstrument)

        coVerify(exactly = 1) { wsHelper.connect("$testUrl?$ARG_SUBSCRIBE=instrument:$testInstrument", any()) }
        coVerify(exactly = 1) { wsHelper.disconnect() }
    }

    companion object {
        val testUrl = "https://aboba.com/"
        val testInstrument = "DOGEUSDT"
    }
}