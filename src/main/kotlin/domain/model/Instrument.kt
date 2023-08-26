package domain.model

data class Instrument(
    val name: String,
    val baseAsset: String,
    val quoteAsset: String,
    val symbol: String,
    val precision: Int
)