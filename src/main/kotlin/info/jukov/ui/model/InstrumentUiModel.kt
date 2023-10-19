
package info.jukov.ui.model

import java.math.BigDecimal

data class InstrumentUiModel(
    val name: String,
    val symbol: String,
    val precision: Int,
    val price: BigDecimal?,
    val priceFormatted: String
)