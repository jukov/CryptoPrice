package domain

import domain.model.Instrument
import domain.model.InstrumentUpdate
import kotlinx.coroutines.flow.Flow


interface TickerRepository {

    suspend fun getInstrumentList(): List<Instrument>?

    suspend fun observeInstrumentUpdates(): Flow<InstrumentUpdate>

    suspend fun subscribe(symbol: String)

    suspend fun subscribe(symbols: List<String>)

    suspend fun unsubscribe(symbol: String)
}