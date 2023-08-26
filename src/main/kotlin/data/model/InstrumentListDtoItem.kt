package data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstrumentListDtoItem(
    @SerialName("quoteCurrency") val quoteCurrency: String?,
    @SerialName("symbol") val symbol: String?,
    @SerialName("timestamp") val timestamp: String?,
    @SerialName("underlying") val underlying: String?
)