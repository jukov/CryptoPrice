package ui

import domain.SettingsRepository
import domain.TickerRepository
import domain.model.Instrument
import domain.model.InstrumentUpdate
import domain.model.UserInstrument
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ui.model.InstrumentPickerUiModel
import ui.model.InstrumentUiModel
import util.DecimalFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class TickerViewModelTest {

    private val decimalFormatter = mockk<DecimalFormatter>()
    private val tickerRepository = mockk<TickerRepository>()
    private val settingsRepository = mockk<SettingsRepository>()

    private val viewModel =
        TickerViewModel(UnconfinedTestDispatcher(), decimalFormatter, tickerRepository, settingsRepository)

    @Test
    fun `add ticker`() = runTest {
        coEvery { tickerRepository.subscribe(testPickerUiModel.symbol) } returns Unit
        coEvery { settingsRepository.setUserInstruments(listOf(testUserInstrument)) } returns Unit

        val updates = ArrayList<List<InstrumentUiModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.observeTickers().collect {
                updates += it
            }
        }

        viewModel.addTicker(testPickerUiModel)

        testScheduler.advanceTimeBy(10L)

        assertEquals(2, updates.size)
        assertEquals(listOf(testInstrumentUiModel), updates.last())

        coVerify(exactly = 1) { tickerRepository.subscribe(testPickerUiModel.symbol) }
        coVerify(exactly = 1) { settingsRepository.setUserInstruments(listOf(testUserInstrument)) }
    }

    @Test
    fun `remove ticker`() = runTest {
        coEvery { tickerRepository.subscribe(testPickerUiModel.symbol) } returns Unit
        coEvery { tickerRepository.unsubscribe(testPickerUiModel.symbol) } returns Unit
        coEvery { settingsRepository.setUserInstruments(listOf(testUserInstrument)) } returns Unit
        coEvery { settingsRepository.setUserInstruments(emptyList()) } returns Unit

        viewModel.addTicker(testPickerUiModel)

        testScheduler.advanceTimeBy(10L)

        viewModel.removeTicker(testPickerUiModel.symbol)

        testScheduler.advanceTimeBy(10L)

        coVerify(exactly = 1) { tickerRepository.unsubscribe(testPickerUiModel.symbol) }
        coVerify(exactly = 1) { settingsRepository.setUserInstruments(emptyList()) }
    }

    @Test
    fun `init`() = runTest {
        coEvery { tickerRepository.observeInstrumentUpdates() } returns MutableSharedFlow()
        coEvery { settingsRepository.getUserInstruments() } returns emptyList()
        coEvery { tickerRepository.subscribe(emptyList()) } returns Unit

        viewModel.init()

        coVerify(exactly = 1) { tickerRepository.observeInstrumentUpdates() }
        coVerify(exactly = 1) { settingsRepository.getUserInstruments() }
        coVerify(exactly = 1) { tickerRepository.subscribe(emptyList()) }
    }

    @Test
    fun `init without user instruments`() = runTest {
        coEvery { tickerRepository.observeInstrumentUpdates() } returns MutableSharedFlow()
        coEvery { settingsRepository.getUserInstruments() } returns null
        coEvery { tickerRepository.subscribe(TickerViewModel.SAMPLE_INSTRUMENTS.map { it.symbol }) } returns Unit

        val updates = ArrayList<List<InstrumentUiModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.observeTickers().collect {
                updates += it
            }
        }

        viewModel.init()

        testScheduler.advanceTimeBy(10L)

        assertEquals(2, updates.size)
        assertEquals(sampleUiModels, updates.last())

        coVerify(exactly = 1) { tickerRepository.subscribe(TickerViewModel.SAMPLE_INSTRUMENTS.map { it.symbol }) }
    }

    @Test
    fun `init with user instruments`() = runTest {
        coEvery { tickerRepository.observeInstrumentUpdates() } returns MutableSharedFlow()
        coEvery { settingsRepository.getUserInstruments() } returns listOf(testUserInstrument)
        coEvery { tickerRepository.subscribe(listOf(testUserInstrument.symbol)) } returns Unit

        val updates = ArrayList<List<InstrumentUiModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.observeTickers().collect {
                updates += it
            }
        }

        viewModel.init()

        testScheduler.advanceTimeBy(10L)

        assertEquals(2, updates.size)
        assertEquals(listOf(testInstrumentUiModel), updates.last())

        coVerify(exactly = 1) { tickerRepository.subscribe(listOf(testUserInstrument.symbol)) }
    }

    @Test
    fun `instrument price change`() = runTest {
        val updateSource = MutableSharedFlow<InstrumentUpdate>()

        coEvery { tickerRepository.observeInstrumentUpdates() } returns updateSource
        coEvery { settingsRepository.getUserInstruments() } returns listOf(testUserInstrument)
        coEvery { tickerRepository.subscribe(listOf(testUserInstrument.symbol)) } returns Unit
        coEvery { decimalFormatter.formatAdjustPrecision(testPrice, any()) } returns testPriceFormatted
        coEvery { decimalFormatter.calcValuePrecision(testPrice, any()) } returns testPrecision

        val updates = ArrayList<List<InstrumentUiModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.observeTickers().collect {
                updates += it
            }
        }

        viewModel.init()

        testScheduler.advanceTimeBy(10L)

        updateSource.emit(testUpdate)

        testScheduler.advanceTimeBy(10L)

        assertEquals(3, updates.size)
        assertEquals(
            listOf(testInstrumentUiModel.copy(price = testPrice, priceFormatted = testPriceFormatted)),
            updates.last()
        )
    }

    @Test
    fun `get available instruments`() = runTest {
        coEvery { tickerRepository.getInstrumentList() } returns testAvailableInstruments

        val instruments = viewModel.getAvailableInstruments().await()

        assertEquals(testInstrumentPickerUiModels, instruments)
    }

    companion object {
        const val testSymbol = "DOGEUSDT"
        val testPrice = "123.0".toBigDecimal()
        const val testPriceFormatted = "123.0"
        const val testPrecision = 1
        val testUserInstrument = UserInstrument(
            "DOGE/USDT",
            testSymbol,
            8
        )
        val testInstrumentUiModel = InstrumentUiModel(
            "DOGE/USDT",
            testSymbol,
            8,
            null,
            TickerViewModel.Companion.PRICE_LOADING
        )
        val testPickerUiModel = InstrumentPickerUiModel(
            "DOGE/USDT",
            "DOGE",
            "USDT",
            testSymbol,
            8
        )
        val testUpdate = InstrumentUpdate(
            testSymbol,
            testPrice
        )

        val sampleUiModels = TickerViewModel.SAMPLE_INSTRUMENTS.map {
            with(it) {
                InstrumentUiModel(
                    name,
                    symbol,
                    precision,
                    null,
                    TickerViewModel.PRICE_LOADING
                )
            }
        }

        val testAvailableInstruments = listOf(
            Instrument("PEPE/USDT", "PEPE", "USDT", "PEPEUSDT", 1),
            Instrument("DOGE/USDT", "DOGE", "USDT", "DOGEUSDT", 1),
            Instrument("PEPE/BTC", "PEPE", "BTC", "PEPEBTC", 1),
        )
        val testInstrumentPickerUiModels = listOf(
            InstrumentPickerUiModel("DOGE/USDT", "DOGE", "USDT", "DOGEUSDT", 1),
            InstrumentPickerUiModel("PEPE/USDT", "PEPE", "USDT", "PEPEUSDT", 1),
        )
    }
}