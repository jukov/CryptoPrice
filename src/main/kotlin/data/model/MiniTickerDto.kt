package data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MiniTickerDto(
    @SerialName("s") val symbol: String?,
    @SerialName("e") val eventType: String?,
    @SerialName("E") val eventTime: Long?,
    @SerialName("h") val high: String?,
    @SerialName("l") val low: String?,
    @SerialName("o") val open: String?,
    @SerialName("c") val close: String?,
    @SerialName("q") val quoteAssetVolume: String?,
    @SerialName("v") val baseAssetVolume: String?
)