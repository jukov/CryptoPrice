package data

import domain.TickerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TickerRepositoryImpl: TickerRepository {

    override fun observeIncrement(): Flow<Int> = flow {
        for (i in 0..1_000_000) {
            emit(i)
            delay(1000L)
        }
    }
}