package domain

import kotlinx.coroutines.flow.Flow


interface TickerRepository {

    fun observeIncrement(): Flow<Int>

}