package info.jukov.domain

import info.jukov.domain.model.Instrument
import info.jukov.domain.model.InstrumentUpdate
import kotlinx.coroutines.flow.Flow


interface TickerRepository {

    suspend fun getInstrumentList(): List<Instrument>?

    suspend fun observeInstrumentUpdates(): Flow<InstrumentUpdate>

    suspend fun observeErrors(): Flow<Throwable>

    suspend fun subscribe(symbol: String)

    suspend fun subscribe(symbols: List<String>)

    suspend fun unsubscribe(symbol: String)

    suspend fun reconnect()
}