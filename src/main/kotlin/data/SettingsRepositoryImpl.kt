package data

import data.model.UserInstrumentDto
import domain.SettingsRepository
import domain.model.UserInstrument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.slf4j.Logger
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsRepositoryImpl(
    val logger: Logger,
    val json: Json
) : SettingsRepository {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getUserInstruments(): List<UserInstrument>? = withContext(Dispatchers.IO) {
        val path = Thread.currentThread().contextClassLoader.getResource("")?.path
        try {
            json.decodeFromStream<List<UserInstrumentDto>>(FileInputStream(path + FILE_NAME))
                .mapNotNull { it.toModel() }
        } catch (e: Exception) {
            logger.error("Error during reading user tickers", e)
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun setUserInstruments(tickers: List<UserInstrument>) {
        withContext(Dispatchers.IO) {
            val path = Thread.currentThread().contextClassLoader.getResource("")?.path
            try {
                json.encodeToStream(
                    tickers.map { it.toDto() },
                    FileOutputStream(path + FILE_NAME)
                )
            } catch (e: Exception) {
                logger.error("Error during writing user tickers", e)
            }
        }
    }

    private fun UserInstrumentDto.toModel(): UserInstrument? {
        return UserInstrument(
            name ?: return null,
            symbol ?: return null,
            precision ?: return null
        )
    }

    private fun UserInstrument.toDto(): UserInstrumentDto {
        return UserInstrumentDto(
            name,
            symbol,
            precision
        )
    }

    companion object {
        private const val FILE_NAME = "settings.xml"
    }
}
