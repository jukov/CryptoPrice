package domain

import domain.model.Instrument
import kotlinx.coroutines.flow.Flow


interface TickerRepository {

    suspend fun observeInstrument(): Flow<Instrument>

    suspend fun subscribe(symbol: String)

    suspend fun unsubscribe(symbol: String)
}