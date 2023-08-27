package data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInstrumentDto(
    val name: String?,
    val symbol: String?,
    val precision: Int?
)