package ui

import domain.TickerRepository
import domain.model.Instrument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TickerViewModel(
    private val repository: TickerRepository
) {

    private var scope = CoroutineScope(Dispatchers.IO)

    suspend fun observePrice(): Flow<Instrument> =
        repository.observeInstrument()

    fun subscribe(symbol: String) {
        scope.launch {
            repository.subscribe(symbol)
        }
    }

    fun unsubscribe(symbol: String) {
        scope.launch {
            repository.unsubscribe(symbol)
        }
    }

}