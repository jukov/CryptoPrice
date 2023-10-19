package info.jukov.data

import info.jukov.data.model.UserInstrumentDto
import info.jukov.domain.SettingsRepository
import info.jukov.domain.model.UserInstrument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.slf4j.Logger
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class SettingsRepositoryImpl(
    val logger: Logger,
    val json: Json
) : SettingsRepository {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getUserInstruments(): List<UserInstrument>? = withContext(Dispatchers.IO) {
        try {
            json.decodeFromStream<List<UserInstrumentDto>>(FileInputStream(FILE_NAME))
                .mapNotNull { it.toModel() }
        } catch (e: FileNotFoundException) {
            logger.info("Config not found, using default settings")
            null
        } catch (e: Exception) {
            logger.error("Error during reading user tickers", e)
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun setUserInstruments(tickers: List<UserInstrument>) {
        withContext(Dispatchers.IO) {
            try {
                json.encodeToStream(
                    tickers.map { it.toDto() },
                    FileOutputStream(FILE_NAME)
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
        private const val FILE_NAME = "CryptoPrice-settings.json"
    }
}
