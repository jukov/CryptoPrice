package data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestDto(
    @SerialName("id") val id: Int,
    @SerialName("method") val method: String,
    @SerialName("params") val params: List<String>
)