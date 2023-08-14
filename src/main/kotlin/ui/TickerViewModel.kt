package ui

import domain.TickerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class TickerViewModel(
    private val repository: TickerRepository
) {

    suspend fun observePrice(): Flow<BigDecimal> =
        repository.observeInstrument()
            .map { it.price }

}