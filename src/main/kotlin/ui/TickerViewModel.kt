package ui

import domain.TickerRepository
import kotlinx.coroutines.flow.Flow

class TickerViewModel(
    private val repository: TickerRepository
) {

    fun observeIncrement(): Flow<Int> = repository.observeIncrement()

}