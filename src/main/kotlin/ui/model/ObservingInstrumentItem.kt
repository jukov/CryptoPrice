
package ui.model

import java.math.BigDecimal

data class ObservingInstrumentItem(
    val name: String,
    val symbol: String,
    val precision: Int,
    val price: BigDecimal?,
    val priceFormatted: String
)