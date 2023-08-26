package domain

import domain.model.Instrument
import kotlinx.coroutines.flow.Flow


interface TickerRepository {

    suspend fun getInstrumentList(): List<Instrument>

    suspend fun observeInstruments(): Flow<Instrument>

    suspend fun subscribe(symbol: String)

    suspend fun unsubscribe(symbol: String)
}