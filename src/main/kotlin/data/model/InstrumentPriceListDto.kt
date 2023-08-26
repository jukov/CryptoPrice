package data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstrumentPriceListDto(
    @SerialName("action") val action: String?,
    @SerialName("data") val `data`: List<InstrumentDto?>?,
    @SerialName("table") val table: String?
) {
    @Serializable
    data class InstrumentDto(
        @SerialName("fairPrice") val fairPrice: Double?,
        @SerialName("symbol") val symbol: String?,
    )
}