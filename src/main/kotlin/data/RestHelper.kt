package data

import data.model.HttpException
import data.model.InstrumentListDtoItem
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.slf4j.Logger

class RestHelper(
    private val logger: Logger,
    private val httpClient: HttpClient,
    private val dataConfig: DataConfig,
    private val json: Json
) {

    suspend fun getInstrumentList(): List<InstrumentListDtoItem> {
        val response = httpClient.get(
            dataConfig.restUrl + "/instrument"
        ) {
            url {
                parameters.append("filter", "{\"typ\": \"IFXXXP\"}")
                parameters.append("columns", "[\"symbol\",\"underlying\",\"quoteCurrency\"]")
                parameters.append("count", 500.toString())
            }
        }

        if (response.status.value in 200..299) {
            return json.decodeFromString<List<InstrumentListDtoItem>>(response.bodyAsText())
        } else {
            val errorBody = try {
                response.bodyAsText()
            } catch (e: Exception) {
                logger.error("errorBody parsing error", e)
                null
            }
            throw HttpException(response.status.value, errorBody)
        }
    }

}