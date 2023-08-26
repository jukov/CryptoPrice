package data

import data.model.HttpException
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.Logger

class RestHelper(
    private val logger: Logger,
    private val httpClient: HttpClient
) {

    suspend fun get(
        url: String,
        urlBuilder: URLBuilder.(URLBuilder) -> Unit
    ): String {
        val response = httpClient.get(url) {
            url(urlBuilder)
        }

        if (response.status.value in 200..299) {
            return response.bodyAsText()
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