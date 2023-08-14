package domain.model

import java.math.BigDecimal

data class Instrument(
    val symbol: String,
    val price: BigDecimal
)