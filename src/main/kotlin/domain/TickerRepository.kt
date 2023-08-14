package domain

import domain.model.Instrument
import kotlinx.coroutines.flow.Flow


interface TickerRepository {

    fun observeIncrement(): Flow<Int>

    suspend fun observeInstrument(): Flow<Instrument>

}