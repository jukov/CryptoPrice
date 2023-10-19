package info.jukov.domain.model

import java.math.BigDecimal

data class InstrumentUpdate(
    val symbol: String,
    val price: BigDecimal
)