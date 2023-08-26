package domain.model

import java.math.BigDecimal

data class Instrument(
    val name: String,
    val symbol: String,
    val price: BigDecimal?
)